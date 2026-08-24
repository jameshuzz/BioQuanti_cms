



package net.mingsoft.mdiy.entity;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.basic.util.BasicUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 自定义模型实体
* @author SMILE
* 创建日期：2019-11-9 15:53:59<br/>
* 历史修订：增加表单的访问地址formUrl<br/>
*/
@TableName("mdiy_model")
public class ModelEntity extends BaseEntity {

private static final long serialVersionUID = 1573286039152L;

	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	* 模型名称
	*/
	@TableField(whereStrategy= FieldStrategy.NOT_EMPTY)
	private String modelName;
	/**
	* 模型表名
	*/
	private String modelTableName;

	/**
	 * 是否能够删除 0-能删除 1-不能删除
	 */
	@TableField(whereStrategy = FieldStrategy.NEVER)
	private int notDel;

	/**
	 * id类型 1自增长 0雪花
	 */
	@TableField(whereStrategy = FieldStrategy.NEVER)
	private int modelIdType;

	/**
	* 类型
	*/
	private String modelType;
	/**
	* 模型业务类型
	*/
	private String modelCustomType;
	/**
	* json
	*/
	private String modelJson;
	/**
	* 自定义字段
	*/
	private String modelField;

	/**
	 * 站点id,非站群下默认为global
	 */
	private String appId;

	/**
	 * 表单的访问地址
	 */
	@TableField(exist = false)
	private String formUrl;

	public String getFormUrl() {
		if(StringUtils.isNotBlank(this.id)){
			formUrl= SecureUtil.aes(SecureUtil.md5(BasicUtil.getApp().getAppId() +"").substring(16).getBytes()).encryptHex(this.id);
		}
		return formUrl;
	}

	public void setFormUrl(String formUrl) {
		this.formUrl = formUrl;
	}

	/**
	* 设置模型名称
	*/
	public void setModelName(String modelName) {
	this.modelName = modelName;
	}

	/**
	* 获取模型名称
	*/
	public String getModelName() {
	return this.modelName;
	}
	/**
	* 设置模型表名
	*/
	public void setModelTableName(String modelTableName) {
	this.modelTableName = modelTableName;
	}

	/**
	* 获取模型表名
	*/
	public String getModelTableName() {
	return this.modelTableName;
	}

	/**
	* 设置类型
	*/
	public void setModelType(String modelType) {
	this.modelType = modelType;
	}

	/**
	* 获取类型
	*/
	public String getModelType() {
	return this.modelType;
	}
	/**
	* 设置模型业务类型
	*/
	public void setModelCustomType(String modelCustomType) {
	this.modelCustomType = modelCustomType;
	}

	/**
	* 获取模型业务类型
	*/
	public String getModelCustomType() {
	return this.modelCustomType;
	}
	/**
	* 设置json
	*/
	public void setModelJson(String modelJson) {
	this.modelJson = modelJson;
	}

	/**
	* 获取json
	*/
	public String getModelJson() {
	return this.modelJson;
	}
	/**
	* 设置自定义字段
	*/
	public void setModelField(String modelField) {
	this.modelField = modelField;
	}

	/**
	* 获取自定义字段
	*/
	public String getModelField() {
	return this.modelField;
	}

	/**
	 * 获取模型字段的映射关系。
	 * 该方法不接受任何参数。
	 *
	 * @return 返回一个Map对象，其中包含模型字段的映射关系。映射关系为模型名称（model）对应字段键（key）。
	 */
	public Map getFieldMap(){
		// 初始化一个空的HashMap用于存储模型字段的映射关系
		Map map=new  HashMap();
		// 将modelField（假设为JSON格式）转换为Map类型的列表
		List<Map> list= JSONUtil.toList(modelField,Map.class);
		// 检查转换后的列表是否不为空
		if(ObjectUtil.isNotNull(list)){
			// 遍历列表中的每个字段映射
			for (Map field : list) {
				// 将模型名称和字段键添加到映射关系中
				map.put(field.get("model"),field.get("key"));
			}
		}
		// 返回包含模型字段映射关系的Map
		return map;
	}

	/**
	 * 获取模型字段和不可重复的映射关系。
	 * 该方法不接受任何参数。
	 * isNoRepeat：true 表示该字段值不可重复，false 表示该字段值可重复。
	 * @return 返回一个Map对象，其中包含模型字段和不可重复的映射关系。映射关系为模型字段名（model）对应该字段是否不可重复（isNoRepeat）。
	 */
	public Map getRepeatMap(){
		// 初始化一个空的HashMap用于存储模型字段的映射关系
		Map map=new  HashMap();
		// 将modelField（假设为JSON格式）转换为Map类型的列表
		List<Map> list= JSONUtil.toList(modelField,Map.class);
		// 检查转换后的列表是否不为空
		if(ObjectUtil.isNotNull(list)){
			// 遍历列表中的每个字段映射
			for (Map field : list) {
				// 将模型名称和是否不可重复添加到映射关系中
				map.put(field.get("model"),ObjectUtil.isNotNull(field.get("isNoRepeat"))?field.get("isNoRepeat"):false);
			}
		}
		// 返回包含模型字段映射关系的Map
		return map;
	}

	public int getNotDel() {
		return notDel;
	}

	public void setNotDel(int notDel) {
		this.notDel = notDel;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public int getModelIdType() {
		return modelIdType;
	}

	public void setModelIdType(int modelIdType) {
		this.modelIdType = modelIdType;
	}

	/**
	 * 获取站点id,如果数据库中存取的是global,那使用getAppId为null<br>
	 * 非站群模式下，导入模型appId默认值为global<br>
	 * 使用场景：目前只有自定义配置复制菜单使用，方便导入自定义配置菜单时，自动拼接appId<br>
	 * @return 站点id
	 */
	public String getAppId() {
		if ("global".equalsIgnoreCase(appId)) {
			return null;
		}
		return appId;
	}

	/**
	 * 设置站点id
	 * @param appId 站点id
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
}
