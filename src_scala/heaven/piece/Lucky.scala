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

package heaven.piece


import org.appdapter.test.CorefTestJava.{_}

class Lucky {
	trait Water {}
	trait CleanWater extends Water {}
	trait Sea [W <: Water] {}
	trait Fish[S <: Sea[CleanWater]] {}
 	trait CleanOceanWithFish[F <: Fish[CleanOceanWithFish[F]]] extends Sea[CleanWater]{}
	trait UnderseaPalace[O,F]{}
// All the above compiles fine, but the below gives:
// error: type arguments [Lucky.this.Zen[T]] do not conform to trait Tao's type parameter bounds [Z <: Lucky.this.Zen[Lucky.this.Tao[Z]]]
// error: type arguments [Lucky.this.Tao[Z]] do not conform to trait Zen's type parameter bounds [T <: Lucky.this.Tao[Lucky.this.Zen[T]]]

	trait Tao[+Z <: Zen[Tao[Z]]]{
		def meditate[M >: Z](m: M) = ()
	};
	trait Zen[+T <: Tao[Zen[T]]]{
		def meditate[M >: T](m: M) = ()
	};

	class Zazen extends Zen[Tao[Zazen]]{}
	// trait Zen[TS <: T, T <: Tao[Zen[TS,TS]]]{};
//	trait Tao[Z <: Zen[Tao[Z],Tao[Z]]]{};
//
//

	trait WildTao[WZ <: WildZen[_]]{
		def meditate(wz : WZ) : Unit;
	};
	trait WildZen[WT <: WildTao[_]]{
	};

	trait ExtendedWildTao[WZ <: WildZen[_]] extends WildTao[WZ] {
 		def meditate(wz : WZ) : Unit = {
			println("meditating on: " + wz);
		} 		
	}
	// Direct Scala extension of the JavaFun Zen&Tao, using unparametric classes, works OK.
	// This is an important baseline of functionality, and from here we can always
	// build a nice type hierarchy in Scala and bring in any useful Java types as members..

	class ShortTao() extends JavaFunTao[ShortZen] {
		override def consider(someZen: ShortZen, otherTao: JavaFunTao[ShortZen]) : ShortZen = {
			println (this.toString() + " is considering " + someZen + " and " + otherTao);
			someZen
		}
	}
	class ShortZen() extends Object with JavaFunZen[ShortTao] {
		override def meditate(someTao: ShortTao, otherZen: JavaFunZen[ShortTao]) : ShortTao = {
			println (this.toString() + " is meditating on " + someTao + " and " + otherZen);
			someTao
		}
	}



  // trait ScalaTao[+SZ <: ScalaZen[ScalaTao[SZ]]] extends JavaEmptyTao[SZ] {	}
  // trait ScalaZen[+ST <: ScalaTao[ScalaZen[ST]]] extends JavaEmptyZen[ST] {	}

/*
 * error: covariant type SEZ occurs in invariant position in type [+SEZ <: com.appdapter.test.CorefTestJava.JavaEmptyZen[com.appdapter.test.CorefTestJava.JavaEmptyTao[SEZ]]]java.lang.Object with com.appdapter.test.CorefTestJava.JavaEmptyTao[SEZ] with ScalaObject{def this(): Lucky.this.ScalaEmptyTao[SEZ]} of class ScalaEmptyTao
        class ScalaEmptyTao[+SEZ <: JavaEmptyZen[JavaEmptyTao[SEZ]]] extends JavaEmptyTao[SEZ] { }
P:\_prj\s2\appdapter\appdapter_trunk\src_scala\heaven\piece\Lucky.scala:51: error: covariant type SEZ occurs in invariant position in type >: Nothing <: com.appdapter.test.CorefTestJava.JavaEmptyZen[com.appdapter.test.CorefTestJava.JavaEmptyTao[SEZ]] of type SEZ
        class ScalaEmptyTao[+SEZ <: JavaEmptyZen[JavaEmptyTao[SEZ]]] extends JavaEmptyTao[SEZ] { }
P:\_prj\s2\appdapter\appdapter_trunk\src_scala\heaven\piece\Lucky.scala:52: error: covariant type SET occurs in invariant position in type [+SET <: com.appdapter.test.CorefTestJava.JavaEmptyTao[com.appdapter.test.CorefTestJava.JavaEmptyZen[SET]]]java.lang.Object with com.appdapter.test.CorefTestJava.JavaEmptyZen[SET] with ScalaObject{def this(): Lucky.this.ScalaEmptyZen[SET]} of class ScalaEmptyZen
        class ScalaEmptyZen[+SET <: JavaEmptyTao[JavaEmptyZen[SET]]] extends JavaEmptyZen[SET] { }
P:\_prj\s2\appdapter\appdapter_trunk\src_scala\heaven\piece\Lucky.scala:52: error: covariant type SET occurs in invariant position in type >: Nothing <: com.appdapter.test.CorefTestJava.JavaEmptyTao[com.appdapter.test.CorefTestJava.JavaEmptyZen[SET]] of type SET
        class ScalaEmptyZen[+SET <: JavaEmptyTao[JavaEmptyZen[SET]]] extends JavaEmptyZen[SET] { }
 */

	// Let's call this "the lo(w) road", where we use the Java types throughout the scala type param bounds.
	// Note that these two Scala type declarations do not mention each other.  They are simply grounded in the
	// JavaEmpty types.
	// The "_ <:" seems to be the magic sugar that allows this construct to be extended in Scala 2.8.1.
	//

	class LoEmptyTao[JEZ <: JavaEmptyZen[  _ <:  JavaEmptyTao [JEZ]]] extends JavaEmptyTao[  JEZ] { }
	class LoEmptyZen[JET <: JavaEmptyTao[  _ <:  JavaEmptyZen [JET]]] extends JavaEmptyZen[  JET] { }
	class ConcLoEmpTao extends LoEmptyTao[ConcLoEmpZen] {
		def love() { println (this.toString() + " feels love")}
	}
	class ConcLoEmpZen extends LoEmptyZen[ConcLoEmpTao] {
		def joy() { println (this.toString() + " knows joy")}
	}

	class LoFunTao[JFZ <: JavaFunZen[  _ <: JavaFunTao [JFZ]]] extends JavaFunTao[  JFZ] {
		// public  consider(JFZ someZen, JavaFunTao<JFZ> otherTao);
		override def consider(someZen: JFZ, otherTao: JavaFunTao[JFZ]) : JFZ = {
			println (this.toString() + " is considering " + someZen + " and " + otherTao);
			someZen
		}
	}
	class LoFunZen[JFT <: JavaFunTao[ _ <:  JavaFunZen [JFT]]] extends JavaFunZen[  JFT] {
		override def meditate(someTao: JFT, otherZen: JavaFunZen[JFT]) : JFT = {
			println (this.toString() + " is meditating on " + someTao + " and " + otherZen);
			someTao
		}
	}

	class ConcLoFunTao extends LoFunTao[ConcLoFunZen] {
		def love(zen : ConcLoFunZen) {
			println (this.toString() + " feels love, and will now consider ")
			val moreZen = consider(zen, this);
		}
	}
	class ConcLoFunZen extends LoFunZen[ConcLoFunTao] {
		def joy(tao : ConcLoFunTao) {
			println (this.toString() + " knows joy, and will now meditate ")
			val moreTao = meditate(tao, this);
		}

	}
	class HiEmptyTao[HEZ <: HiEmptyZen[  _ <:  HiEmptyTao [HEZ]]] extends JavaEmptyTao[  HEZ] { }
	class HiEmptyZen[HET <: HiEmptyTao[  _ <:  HiEmptyZen [HET]]] extends JavaEmptyZen[  HET] { }

	class HiFunTao[HFZ <: HiFunZen[  _ <: HiFunTao [HFZ]]] extends JavaFunTao[ HFZ] {
		override def consider(someZen: HFZ, otherTao: JavaFunTao[HFZ]) : HFZ = {
			println (this.toString() + " is considering " + someZen + " and " + otherTao);
			someZen
		}
	}
	class HiFunZen[HFT <: HiFunTao[ _ <:  HiFunZen [HFT]]] extends JavaFunZen[ HFT] {
		override def meditate(someTao: HFT, otherZen: JavaFunZen[HFT]) : HFT = {
			println (this.toString() + " is meditating on " + someTao + " and " + otherZen);
			someTao
		}
	}
	class ConcHiFunTao extends HiFunTao[ConcHiFunZen] {
		def love(zen : ConcHiFunZen) {
			println (this.toString() + " feels love, and will now consider ")
			val moreZen = consider(zen, this);
		}
	}
	class ConcHiFunZen extends HiFunZen[ConcHiFunTao] {
		def joy(tao : ConcHiFunTao) {
			println (this.toString() + " knows joy, and will now meditate ")
			val moreTao = meditate(tao, this);
		}

	}
	class SplitTao extends HiFunTao[SplitZen] {
		def love(zen : SplitZen) {
			println (this.toString() + " feels love, and will now consider ")
			val moreZen = consider(zen, this);
		}
	}
	class SplitZen extends HiFunZen[SplitTao] {
		def joy(tao : SplitTao) {
			println (this.toString() + " knows joy, and will now meditate ")
			val moreTao = meditate(tao, this);
		}

	}

	abstract class LoSpec {
		type SpecLET = LoEmptyTao[SpecLEZ];
		type SpecLEZ <: LoEmptyZen[SpecLET];
		def fire(tao : SpecLET , zen : SpecLEZ) : Unit = {
			
		}
	}

	class GoLoSpec extends LoSpec {
		// type SpecLEZ = LoEmptyZen[SpecLET];

		def go() {
		}
	}
	//trait ScalaRawTao[SZ <: ScalaRawZen[_ <: ScalaRawTao[SZ]]] extends JavaRawTao[SZ] {	}
	//trait ScalaRawZen[ST <: ScalaRawTao[_ <: ScalaRawZen[ST]]] extends JavaRawZen[ST] {	}
}
object LuckyTest extends Lucky {
	def test() : Unit = {
		val st = new ShortTao();
		val sz = new ShortZen();

		val moreST = sz.meditate(st, sz);

		val cleTao = new ConcLoEmpTao();
		val cleZen = new ConcLoEmpZen();
		cleTao.love();
		cleZen.joy();

		val clfTao = new ConcLoFunTao();
		val clfZen = new ConcLoFunZen();
		clfTao.love(clfZen);
		clfZen.joy(clfTao);

		val chfTao = new ConcHiFunTao();
		val chfZen = new ConcHiFunZen();
		chfTao.love(chfZen);
		chfZen.joy(chfTao);
	}
}

	// Now let's look at "the hi(gh) road", where the scala type params do reference each other, and
	// we only use the Java types as the inheritance targets.
	// This does not compile for me yet, with or without the variance notations.
	// class ScalaHiEmptyTao[ +SEZ <: ScalaHiEmptyZen [ ScalaHiEmptyTao [ SEZ]]] extends JavaEmptyTao[SEZ] { }
	// class ScalaHiEmptyZen[ +SET <: ScalaHiEmptyTao [ ScalaHiEmptyZen [ SET]]] extends JavaEmptyZen[SET] { }

	// class ScalaPlainEmptyTao() extends ScalaEmptyTao[ScalaPlainEmptyZen] {}
	// class ScalaPlainEmptyZen() extends ScalaEmptyZen[ScalaPlainEmptyTao] {}  // extends JavaEmptyZen[ScalaPlainEmptyTao]
	//



	// val spet = new ScalaPlainEmptyTao();
	// val spez = new ScalaPlainEmptyZen();


	// Now, suppose I want to instantiate these classes.  Can I do it using type wildcards?
	// val set = new ScalaEmptyTao[ScalaEmptyZen[_]]();
	// val sez = new ScalaEmptyZen[ScalaEmptyTao[_ <: ScalaEmptyZen]]();
	
	// What about without using wildcards?   Perhaps the answer is to use abstract types.
	
	// type SpecificSET <: ScalaEmptyTao[SpecificSEZ];
	// type SpecificSEZ <: ScalaEmptyZen[SpecificSET];

// class ConcreteTao() extends ScalaTao[ScalaZen[ScalaTao[ScalaZen[_]]]] {
//
// }
/*
	val set = new ScalaEmptyTao[ScalaEmptyZen[]();
	val sez = new ScalaEmptyZen[ScalaTao[_]]();
	sz.meditate(st);
*/
//	trait ScalaTao[+SZ <: ScalaZen[ScalaTao[SZ]]] extends JavaTao[SZ] {	}
//	trait ScalaZen[+ST <: ScalaTao[ScalaZen[ST]]] extends JavaZen[ST] {	}
	// trait ScalaTao extends JavaTao[JavaZen[ScalaTao]] {
	// }
// trait ScalaTao[+SZ <: ] extends JavaTao[Z] {}
	/*
	class TriggerYes[B <: Box[_]] extends  TriggerImpl[B] {
		def fire(targetBox : B) : Unit = {
			println("TriggerOne is firing on box: " + targetBox);
		}
	}
	*/
	// Stick the magical abstract types here, please, if needed.
	// trait TriggerTrait[BT <: Box[TriggerTrait[BT]]] { }
	// type	TriggerType <: Trigger[B forSome{type B <: Box[TriggerType]}]; // <: Trigger[_];
	// type	BoxType <: Box[TriggerType];
	//
	//
	//
	/*
	trait TriggerThree[X forSome {type X}];
	class TriggerTwo[BT forSome {type BT}] () { // extends TriggerImpl[BT]  {
		def fire(targetBox : BT) : Unit = {
			println("TriggerTwo is firing on box: " + targetBox);
		}
	}
	*/
	/*
	class TriggerOne() extends  TriggerImpl {
		def fire(targetBox : Box[_]) : Unit = {
			println("TriggerOne is firing on box: " + targetBox);
		}
	}
	*/
