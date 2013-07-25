package org.appdapter.core.convert;

public interface TypeAssignable {

	public int WONT = 1;
	public int MIGHT = 0;
	public int WILL = -1;

	public Integer declaresConverts(Object val, Class valClass, Class objNeedsToBe);

	public TypeAssignable CASTING_ONLY = new TypeAssignable() {
		public Integer declaresConverts(Object val, Class have, Class must) {
			if (must == null || have == null)
				return WILL;
			must = ReflectUtils.nonPrimitiveTypeFor(must);
			have = ReflectUtils.nonPrimitiveTypeFor(have);
			if (ReflectUtils.isDisjointTypes(must, have))
				return WONT;
			if (must.isAssignableFrom(have))
				return WILL;
			if (have.isAssignableFrom(must))
				return WILL;
			if (must != have)
				return MIGHT;
			return WILL;
		}
	};
	public TypeAssignable PERFECT = new TypeAssignable() {
		public Integer declaresConverts(Object val, Class have, Class must) {
			if (must == null || have == null)
				return WILL;
			if (ReflectUtils.isSameType(have, must))
				return WILL;
			return WONT;
		}
	};
	public TypeAssignable ANY = new TypeAssignable() {
		@Override public Integer declaresConverts(Object val, Class valClass, Class objNeedsToBe) {
			return WILL;
		}

	};

}
