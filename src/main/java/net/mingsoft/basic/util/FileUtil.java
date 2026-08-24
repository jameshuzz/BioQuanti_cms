







package net.mingsoft.basic.util;

import cn.hutool.core.io.FileMagicNumber;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.validation.constraints.NotNull;
import net.mingsoft.base.entity.BaseEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: FileUtil
 * @Description: TODO(清理文件)
 * @author 铭软开发团队
 * @date 2018年7月29日
 *
 */
public class FileUtil {

	private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * 文件后缀
	 */
	private static String fileSuffix = "[/|\\\\]upload.*?\\.(rmvb|mpga|mpg4|mpeg|docx|xlsx|pptx|jpeg|[a-z]{3})";

	/**
	 * @Title: del
	 * @Description: TODO(查找出json数据里面的附件路径，并执行删除)
	 * @param json
	 *            通常是业务实体转换之后的json字符串
	 */
	public static void del(String json) {
		Pattern pattern = Pattern.compile(fileSuffix);
		Matcher matcher = pattern.matcher(json);
		while (matcher.find()) {
			try {
				FileUtils.forceDelete(new File(BasicUtil.getRealPath(matcher.group())));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Title: del
	 * @Description: TODO(查找出list数据里面的附件路径，并执行删除)
	 * @param list
	 *            对象集合
	 */
	public static void del(List<?> list) {
		String json = "";
		for (Object entity : list) {
			json = JSONUtil.toJsonStr(entity);
			FileUtil.del(json);
		}
	}

	/**
	 * @Title: del
	 * @Description: TODO(查找出实体数据里面的附件路径，并执行删除)
	 * @param entity
	 *            实体对象
	 */
	public static void del(BaseEntity entity) {
		String json = JSONUtil.toJsonStr(entity);
		FileUtil.del(json);
	}


	/**
	 * 读取jar包中的文件
	 * @param path 文件路径
	 * @return 文件内容
	 */
	public static String readJarFile(String path) {
		try {
			ClassPathResource classPathResource = new ClassPathResource(path);
			return IOUtils.toString(classPathResource.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 验证文件名是否非法
	 * @param fileName 要检查的文件名
	 * @return true 表示非法，false 表示合法
	 */
	public static boolean isInvalidFileName(String fileName) {
		// 1. 验证文件名是否为空
		if (StrUtil.isBlank(fileName)) {
			return true;
		}

		// 2. 禁止路径穿越：包含 ../ 或 ..\ 等路径穿越字符
		if (fileName.contains("../") || fileName.contains("..\\") || fileName.contains("/..")
				|| fileName.contains("\\..") || fileName.equals("..") || fileName.equals(".")
				|| fileName.contains("./") || fileName.contains("\\.")) {
			return true;
		}

		// 3. 禁止绝对路径开头的文件名
		if (fileName.matches("^[a-zA-Z]:[\\/\\\\].*") ||  // Windows驱动器路径，如 C:\
				fileName.startsWith("/") ||           // Unix/Linux绝对路径，如 /etc/passwd
				fileName.startsWith("\\")) {          // Windows绝对路径，如 \Windows
			return true;
		}

		return false;
	}

	/**
	 * 将字节数组转换成BytesToMultipartFile，其中BytesToMultipartFile继承MultipartFile
	 * @param bytes 字节数组
	 * @param defaultSuffix 默认文件后缀，当通过流获取不到时，将使用这个为后缀名
	 * @return MultipartFile
	 */
	public static MultipartFile bytesToMultipartFile(byte[] bytes, String defaultSuffix) {
		// 尝试从流中获取后缀地址
		FileMagicNumber fileMagicNumber = FileMagicNumber.getMagicNumber(bytes);
		String suffix = fileMagicNumber.getExtension();
		// 如果后缀为空则使用默认后缀
		suffix = StrUtil.isNotBlank(suffix) ? suffix : defaultSuffix;
		String fileName = IdUtil.getSnowflake().nextId() + "." + suffix;
		return FileUtil.bytesToMultipartFile(bytes, fileName, fileMagicNumber.getMimeType());
	}

	/**
	 * 将字节数组转换成BytesToMultipartFile，其中BytesToMultipartFile继承MultipartFile
	 * @param bytes 字节数组
	 * @param fileName 文件名称
	 * @param contentType 文件类型 如image/jpeg、image/png等
	 * @return MultipartFile
	 */
	public static MultipartFile bytesToMultipartFile(byte[] bytes, String fileName, String contentType) {
		return new BytesToMultipartFile(bytes, fileName, contentType);
	}


	private static class BytesToMultipartFile implements MultipartFile {

		// 文件字节数组
		private final byte[] bytes;
		// 文件名称
		private final String fileName;
		// 文件类型
		private final String contentType;

		public BytesToMultipartFile(byte[] bytes, String fileName, String contentType) {
			this.bytes = bytes;
			this.fileName = fileName;
			this.contentType = contentType;
		}
		@NotNull
		@Override public String getName() { return fileName; }

		@NotNull
		@Override public String getOriginalFilename() { return fileName; }

		@NotNull
		@Override public String getContentType() { return contentType; }

		@Override public boolean isEmpty() { return bytes == null || bytes.length == 0; }

		@Override public long getSize() { return bytes.length; }

		@NotNull
		@Override public byte[] getBytes() { return bytes; }

		@NotNull
		@Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }

		@Override
		public void transferTo(@NotNull File dest) throws IOException {
			try (OutputStream os = new FileOutputStream(dest)) {
				os.write(bytes);
			}
		}
	}
}
