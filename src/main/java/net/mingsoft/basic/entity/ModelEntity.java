








package net.mingsoft.basic.entity;

import com.baomidou.mybatisplus.annotation.*;
import net.mingsoft.base.constant.e.BaseEnum;
import net.mingsoft.base.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * 模块实体
 * @author ms dev group
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
@TableName("model")
public class ModelEntity extends BaseEntity {

	@TableId(type = IdType.AUTO)
	private String id;

	@Override
	public String getId() {
		if(StringUtils.isEmpty(this.id) || this.id.equals("0")){
			return null;
		}
		return id;
	}

	@Override
	public void setId(String id) {
		if(StringUtils.isEmpty(id) || id.equals("0")){
			id = null;
		}
		this.id = id;
	}

	/**
     * 模块的标题
     */
    private String modelTitle;

    /**
     * 发布时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp modelDatetime;

    /**
     * 模块父id
     */
	@TableField(updateStrategy = FieldStrategy.ALWAYS)
	private Integer modelId;

    /**
     * 链接地址
     */
    private String modelUrl;

    /**
     * 模块编码
     */
    private String modelCode;
    /**
	 * 菜单类型
	 */
    private String isChild;

    /**
     * 模块图标
     */
    private String modelIcon = null;


    /**
     * 模块排序
     * @return
     */
    private Integer modelSort;
    /**
     * 子功能集合，不参加表结构
     */
	@TableField(exist = false)
	private List<ModelEntity> modelChildList;
    /**
     * 是否是菜单，数据库字段是model_ismuenu
     */
    @TableField(value = "model_ismenu")
    private Integer modelIsMenu;
    /**
     * 选中状态，不参加表结构
     */
	@TableField(exist = false)
	private int chick;

    /**
     * 树的深度，不参加表结构
     */
	@TableField(exist = false)
	private int depth;
    /**
     * 父级编号集合
     * @return
     */
	@TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String modelParentIds;

	/**
	 * 站点编号，正常情况下appId是没有值的，只有在站群下复制自定义配置才会有值，且导入菜单
	 */
	private String appId;

	/**
	 *
	 * 获取层级
	 * @return
	 */
	public int getDepth() {
		if(StringUtils.isNotEmpty(modelParentIds)){
			return depth = modelParentIds.split(",").length;
		}else {
			return depth;
		}

	}

	/**
	 *
	 * 设置层级
	 * @param depth
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	public Integer getModelIsMenu() {
		return modelIsMenu;
	}

	public void setModelIsMenu(Integer modelIsMenu) {
		this.modelIsMenu = modelIsMenu;
	}


    /**
     * 获取modelCode
     * @return modelCode
     */
    public String getModelCode() {
        return modelCode;
    }

    /**
     * 设置modelCode
     * @param modelCode
     */
    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    /**
     * 获取modelIcon
     * @return modelIcon
     */
    public String getModelIcon() {
        return modelIcon;
    }

    /**
     * 设置modelIcon
     * @param modelIcon
     */
    public void setModelIcon(String modelIcon) {
        this.modelIcon = modelIcon;
    }


    /**
     * 获取modelUrl
     * @return modelUrl
     */
    public String getModelUrl() {
        return modelUrl;
    }

    /**
     * 设置modelUrl
     * @param modelUrl
     */
    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }


    /**
     * 获取modelDatetime
     * @return modelDatetime
     */
    public Timestamp getModelDatetime() {
        return modelDatetime;
    }

    /**
     * 设置modelDatetime
     * @param modelDatetime
     */
    public void setModelDatetime(Timestamp modelDatetime) {
        this.modelDatetime = modelDatetime;
    }

    /**
     * 获取modelTitle
     * @return modelTitle
     */
    public String getModelTitle() {
        return modelTitle;
    }

    /**
     * 设置modelTitle
     * @param modelTitle
     */
    public void setModelTitle(String modelTitle) {
        this.modelTitle = modelTitle;
    }


	public Integer getModelSort() {
		return modelSort;
	}

	public void setModelSort(Integer modelSort) {
		this.modelSort = modelSort;
	}

	public List<ModelEntity> getModelChildList() {
		return modelChildList;
	}

	public void setModelChildList(List<ModelEntity> modelChildList) {
		this.modelChildList = modelChildList;
	}

	public int getChick() {
		return chick;
	}

	public void setChick(int chick) {
		this.chick = chick;
	}


	public String getModelParentIds() {
		return modelParentIds;
	}

	public void setModelParentIds(String modelParentIds) {
		this.modelParentIds = modelParentIds;
	}

	/**
	 * 获取菜单类型
	 */
	public String getIsChild() {
		return isChild;
	}
	/**
	 * 设置菜单类型
	 */
	public void setIsChild(String isChild) {
		this.isChild = isChild;
	}

	public Integer getModelId() {
		return modelId;
	}

	public void setModelId(Integer modelId) {
		this.modelId = modelId;
	}


	public enum IsMenu implements BaseEnum{
		NO(0),
		YES(1);
		private int id;
		IsMenu(int id){
			this.id = id;
		}
		@Override
		public int toInt() {
			
			return this.id;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return this.id.equals(((ModelEntity)o).getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * 获取appId
	 */
	public String getAppId() {
		return appId;
	}

	/**
	 * 设置appId
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
}
