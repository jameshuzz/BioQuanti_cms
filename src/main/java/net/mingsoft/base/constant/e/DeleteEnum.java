









package net.mingsoft.base.constant.e;

/**
 * 
 * @ClassName:  DeleteEnum   
 * @Description:TODO(删除枚举)
 * @date:   2018年3月19日 下午3:34:02   
 *     
 */
public enum DeleteEnum implements BaseEnum{
	/**
	 * 伪删除（NOTDEL正常,值为0）
	 */
	NOTDEL(0,"正常"),

	/**
	 * 伪删除（DEL已删除,值为1）
	 */
	DEL(1,"已删除");
	

	
	private String code;
	
	private int id;

	/**
	 * 构造方法
	 * @param id 默认ID
	 * @param code 传入的枚举类型
	 */
	DeleteEnum(int id,String code) {
		this.code = code;
		this.id = id;
	}

	@Override
	public int toInt() {
		return this.id;
	}

	@Override
	public String toString() {
		return this.code.toString();
	}
}
