package net.mingsoft.cms.biz.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.cms.biz.ICategoryBiz;
import net.mingsoft.cms.biz.IContentBiz;
import net.mingsoft.cms.biz.IIntroTplBiz;
import net.mingsoft.cms.dao.IIntroTplDao;
import net.mingsoft.cms.entity.CategoryEntity;
import net.mingsoft.cms.entity.ContentEntity;
import net.mingsoft.cms.entity.IntroTplEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 产品简介模板业务实现
 * 骨架HTML由服务端统一拼装，保证左列结构全站一致：
 * <section class="bq-intro" data-tpl="id"><table class="bq-introtab"><tbody>
 *   <tr><th>标签</th><td>默认值</td></tr>
 * </tbody></table></section>
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-09-18<br/>
 */
@Service("introTplBizImpl")
@Transactional
public class IntroTplBizImpl extends BaseBizImpl<IIntroTplDao, IntroTplEntity> implements IIntroTplBiz {

	@Autowired
	private IIntroTplDao introTplDao;

	@Autowired
	private ICategoryBiz categoryBiz;

	@Autowired
	private IContentBiz contentBiz;

	@Override
	protected IBaseDao getDao() {
		return introTplDao;
	}

	@Override
	public void saveTpl(IntroTplEntity entity, String rows) {
		if (StringUtils.isBlank(entity.getTplName())) {
			throw new BusinessException("模板名称不能为空");
		}
		if (StringUtils.isBlank(entity.getCategoryIds())) {
			throw new BusinessException("请选择绑定的栏目");
		}
		List<String[]> rowList = parseRows(rows);
		if (rowList.isEmpty()) {
			throw new BusinessException("请至少添加一行简介信息");
		}

		// 栏目绑定唯一：一个栏目只能绑一个模板（排除自身）
		for (IntroTplEntity other : this.list()) {
			if (other.getId().equals(entity.getId())) {
				continue;
			}
			for (String cid : entity.getCategoryIds().split(",")) {
				if (StringUtils.isNotBlank(cid) && Arrays.asList(other.getCategoryIds().split(",")).contains(cid)) {
					CategoryEntity cat = categoryBiz.getById(cid.trim());
					throw new BusinessException("栏目「" + (cat == null ? cid : cat.getCategoryTitle()) + "」已绑定模板「"
							+ other.getTplName() + "」，一个栏目只能绑定一个简介模板");
				}
			}
		}

		// 新增先生成id，骨架 data-tpl 需要它
		if (StringUtils.isBlank(entity.getId())) {
			entity.setId(cn.hutool.core.util.IdUtil.getSnowflakeNextIdStr());
		}
		entity.setContent(buildContent(entity.getId(), rowList));
		if (this.getById(entity.getId()) == null) {
			this.saveEntity(entity);
		} else {
			this.updateEntity(entity);
		}
	}

	@Override
	public IntroTplEntity getByCategory(String categoryId, String linkId) {
		String cid = StringUtils.isNotBlank(categoryId) ? categoryId.trim() : "";
		// 编辑场景：优先按文章所属栏目解析
		if (StringUtils.isNotBlank(linkId)) {
			ContentEntity article = contentBiz.getById(linkId.trim());
			if (article != null && StringUtils.isNotBlank(article.getCategoryId())) {
				cid = article.getCategoryId();
			}
		}
		if (StringUtils.isBlank(cid)) {
			return null;
		}
		// 栏目链：自身 + 所有父级（模板绑父栏目时子栏目文章同样命中）
		List<String> chain = new ArrayList<>();
		chain.add(cid);
		CategoryEntity cat = categoryBiz.getById(cid);
		if (cat != null && StringUtils.isNotBlank(cat.getCategoryParentIds())) {
			chain.addAll(Arrays.asList(cat.getCategoryParentIds().split(",")));
		}
		for (IntroTplEntity tpl : this.list()) {
			if (StringUtils.isBlank(tpl.getCategoryIds())) {
				continue;
			}
			for (String bound : tpl.getCategoryIds().split(",")) {
				if (StringUtils.isNotBlank(bound) && chain.contains(bound.trim())) {
					return tpl;
				}
			}
		}
		return null;
	}

	/**
	 * 行数据JSON转二维数组
	 */
	private List<String[]> parseRows(String rows) {
		List<String[]> result = new ArrayList<>();
		if (StringUtils.isBlank(rows)) {
			return result;
		}
		try {
			JSONArray arr = JSONUtil.parseArray(rows);
			for (Object o : arr) {
				JSONObject jo = (JSONObject) o;
				String label = jo.getStr("label", "");
				String value = jo.getStr("value", "");
				if (StringUtils.isBlank(label)) {
					continue;
				}
				result.add(new String[]{label.trim(), value == null ? "" : value.trim()});
			}
		} catch (Exception e) {
			throw new BusinessException("行数据格式错误");
		}
		return result;
	}

	/**
	 * 拼装规整骨架HTML
	 */
	private String buildContent(String id, List<String[]> rows) {
		StringBuilder sb = new StringBuilder();
		// H3"产品简介"标题：详情页富文本H3路由与产品简介模块展示均依赖它
		sb.append("<h3>产品简介</h3>");
		sb.append("<section class=\"bq-intro\" data-tpl=\"").append(id == null ? "" : id).append("\">");
		sb.append("<table class=\"bq-introtab\"><tbody>");
		for (String[] r : rows) {
			sb.append("<tr><th>").append(escapeHtml(r[0])).append("</th><td>");
			sb.append(StringUtils.isBlank(r[1]) ? "&nbsp;" : escapeHtml(r[1]));
			sb.append("</td></tr>");
		}
		sb.append("</tbody></table></section>");
		return sb.toString();
	}

	private String escapeHtml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
