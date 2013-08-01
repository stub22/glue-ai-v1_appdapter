package org.appdapter.core.convert;

public interface TypeAssignable {

	public int WONT = 1;
	public int MIGHT = 0;
	public int WILL = -1;

	public Integer declaresConverts(Object val, Class valClass, Class objNeedsToBe, int maxConverts);

	public String toString();

	public TypeAssignable CASTING_ONLY = new TypeAssignable() {
		public Integer declaresConverts(Object val, Class have, Class must, int maxConverts) {
			if (must == null || have == null)
				return WILL;
			must = ReflectUtils.nonPrimitiveTypeFor(must);
			have = ReflectUtils.nonPrimitiveTypeFor(have);
			if (ReflectUtils.isDisjointTypes(must, have))
				return WONT;
			if (must.isAssignableFrom(have))
				return MIGHT;
			if (have.isAssignableFrom(must))
				return WILL;
			if (must != have)
				return MIGHT;
			return WILL;
		}

		@Override public String toString() {
			return "CASTING_ONLY";
		}
	};
	public TypeAssignable PERFECT = new TypeAssignable() {
		public Integer declaresConverts(Object val, Class have, Class must, int maxConverts) {
			if (must == null || have == null)
				return WILL;
			if (ReflectUtils.isSameType(have, must))
				return WILL;
			return WONT;
		}

		@Override public String toString() {
			return "PERFECT";
		}
	};
	public TypeAssignable ANY = new TypeAssignable() {
		@Override public Integer declaresConverts(Object val, Class valClass, Class objNeedsToBe, int maxConverts) {
			return WILL;
		}

		@Override public String toString() {
			return "ANY (Ignored)";
		}

	};
	public int MCVT = 3;

}
