/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.appdapter.bind.math.jscience.probability;

import java.util.ArrayList;
import java.util.List;

import javolution.text.Text;

import org.appdapter.bind.math.jscience.function.VariableFuncs;
import org.appdapter.bind.math.jscience.number.FieldNumberFactory;
import org.jscience.mathematics.function.Function;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.FieldNumber;

/**
 *
 * @author winston
 */
public abstract class ProbabilityDensityFunction<DomainValue, MeasureNumber extends FieldNumber<MeasureNumber>>
				extends Function<DomainValue, MeasureNumber> {

	private		List<Variable<DomainValue>>		myVars;

	public		FieldNumberFactory<MeasureNumber>		myMeasureNumberFactory;

	protected ProbabilityDensityFunction(FieldNumberFactory<MeasureNumber> measNumFact) {
		myMeasureNumberFactory = measNumFact;
	}
	public final void setVariables(List<Variable<DomainValue>> vars) {
		myVars = vars;
	}
	@Override public final List<Variable<DomainValue>> getVariables() {
		return myVars;
	}
	@Override public Text toText() {
		return new Text(getClass().getSimpleName() + "[curVal=" + evaluate() + ", curVars=[" + VariableFuncs.dumpVarList(myVars) + "]]");
	}
	public final double evaluateToDouble() {
		MeasureNumber result = evaluate();
		if (result == null) {
			throw new RuntimeException ("evaluateToDouble() got null result from evaluate()");
		}
		return result.doubleValue();
	}
	public final List<DomainValue> getCurrentDomainVariableValues() {
		List<DomainValue> res = new ArrayList<DomainValue>();
		for (Variable<DomainValue> v : myVars) {
			res.add(v.get());
		}
		return res;
	}
}
