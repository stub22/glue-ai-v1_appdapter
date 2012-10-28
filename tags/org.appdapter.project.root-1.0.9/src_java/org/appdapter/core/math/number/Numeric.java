/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.appdapter.core.math.number;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface Numeric<N> {
	// More good than harm?  1) impl burden?, 2) what does a "no" answer imply? (open/closed world)

	public boolean isScalar();  // Do we treat complex as a scalar?

	public boolean isZero();   // All components zero

	public boolean isPositive();  // All components positive

	public boolean isNonnegative(); // All components nonnegative

	public boolean isFinite(); // All components finite

	public Nonnegative<N> asNonnegative();
	public Positive<N> asPositive();
	public Zero<N> asZero();
	public Finite<N> asFinite();

	public abstract class Basic<BN> implements Numeric<BN> {
		protected RuntimeException downcastFailureException(Class targetType) {
			return new RuntimeException("Cannot treat [" + this + "] as " + targetType);
		}
	}

	/* Implies factory capabilities
	public boolean hasAdditiveInverse();
	public N getAdditiveInverse(); // returns null iff hasAdditiveInverse() returns false
	public boolean hasAbsoluteValue();
	public N getAbsoluteValue();
	 */
	public interface Nonnegative<NNN> extends Numeric<NNN> {
		public abstract class Basic<BNNN> extends Numeric.Basic<BNNN> implements Nonnegative<BNNN> {
			@Override public boolean isNonnegative()			{	return true;	}
			@Override public Nonnegative<BNNN> asNonnegative()	{	return this;	}
		}
	}

	public interface Positive<PN> extends Nonnegative<PN> {

		public abstract class Basic<BPN> extends Nonnegative.Basic<BPN> implements Positive<BPN> {

			@Override public boolean isPositive()		{	return true;	}

			@Override public boolean isZero()			{	return false;	}
			@Override public Positive<BPN> asPositive()	{	return this;	}
			@Override public Zero<BPN> asZero()			{	throw downcastFailureException(Zero.class);	}
		}
	}

	public interface Finite<FN> extends Numeric<FN> {
		public interface Nonnegative<NNFN> extends Finite<NNFN>, Numeric.Nonnegative<NNFN> {
			public abstract class Basic<BNNFN> extends Numeric.Nonnegative.Basic<BNNFN> implements Finite.Nonnegative<BNNFN> {
				@Override public boolean isFinite()		{return true; }
				@Override public Finite<BNNFN> asFinite() {return this; }
			}
		}

		public interface Positive<PFN> extends Finite.Nonnegative<PFN>, Numeric.Positive<PFN> {
			public abstract class Basic<BPFN> extends Numeric.Positive.Basic<BPFN> implements Finite.Positive<BPFN> {
				@Override	public boolean isFinite()			{return true;}
				@Override	public Finite<BPFN> asFinite()		{return this;}
			}
		}
	}

	public interface Zero<ZN> extends Nonnegative<ZN>, Finite<ZN> {

		public abstract class Basic<BZN> extends Numeric.Nonnegative.Basic<BZN> implements Zero<BZN> {

			@Override public boolean isZero()			{	return true;	}
			@Override public boolean isPositive()		{	return false;	}
			@Override public boolean isFinite()			{	return true;	}

			@Override public Zero<BZN> asZero()			{	return this;	}
			@Override public Finite<BZN> asFinite()		{	return this;	}

			@Override public Positive<BZN> asPositive() {	throw downcastFailureException(Positive.class);	}

		}
	}

	public interface Infinite<IN> extends Numeric<IN> {
		public abstract class Basic<BIN> extends Numeric.Basic<BIN> implements Finite.Nonnegative<BIN> {

			@Override public boolean isFinite()		{	return false;		}
			@Override public Finite<BIN> asFinite() {	throw downcastFailureException(Finite.class); }
		}
	}
	// Integers and Reals are definitely Scalars.  Haven't decided re: Complex, but probably.
	public interface Scalar<SN> extends Numeric<SN> {
		public interface Finite<FSN> extends Scalar<FSN>, Numeric.Finite<FSN> {
			
		}
	}
	public interface Comparable<CN> extends java.lang.Comparable<CN> {
		
	}
}
