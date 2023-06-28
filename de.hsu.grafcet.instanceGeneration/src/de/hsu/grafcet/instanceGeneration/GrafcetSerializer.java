package de.hsu.grafcet.instanceGeneration;

import java.util.ArrayList;
import java.util.List;

import de.hsu.grafcet.*;
import terms.Addition;
import terms.And;
import terms.BooleanConstant;
import terms.IntegerConstant;
import terms.Not;
import terms.Sort;
import terms.Term;
import terms.TermsFactory;
import terms.Variable;
import terms.VariableDeclaration;
import terms.VariableDeclarationType;

public class GrafcetSerializer {

	private Grafcet grafcet;
	private GrafcetFactory facG;
	private TermsFactory facT;
	private int n;
	private int m;
	private int countTransition;
	private int countStep = 1;
	private List<VariableDeclaration> inputVars = new ArrayList<VariableDeclaration>();
	
	private int getStepCount(){
		countStep ++;
		return countStep -1;
	}
	
	/**
	 * @param instanceType type of instence to generate
	 * @param m item to iterate over
	 * @param n item to set a fixed size; might not be used
	 */
	public GrafcetSerializer(GrafcetSerializerInstanceType instanceType, int m, int n) {
		serializeGrafcetShell();
		this.n = n;
		this.m = m;
		createInputVariables(instanceType);
		serializeInstance(instanceType);
	}
	
	private void createInputVariables(GrafcetSerializerInstanceType instanceType) {
		int noOfInputVars = (int) Math.ceil(Math.log(getTransitionNo(instanceType)) / Math.log(2));
		for (int i = 1; i <= noOfInputVars; i ++) {
			inputVars.add(createVariableDeclaration("in" + i, facT.createBool(), 
					VariableDeclarationType.INPUT));
		}
	}
	
	private double getTransitionNo(GrafcetSerializerInstanceType instanceType) {
		int transitionNo = 0;
		switch (instanceType) {
		case BASIC_SEQUENCE: {
			transitionNo = m;
		} break;
		case BASIC_PARALLEL: {
			double am1 = 3;
			for (int i = 1; i <= m; i++) {
				am1 = am1 + 2 * Math.pow(n, i); 
			}
			transitionNo = (int) am1;
		} break;
		case BASIC_VARIABLES: {
			transitionNo = n;
		} 
		break;
		case HIERARCHICAL_SEQUENCE:
		case HIERARCHICAL_PARALLEL: {
			transitionNo = n * m;
		} 
		break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + instanceType);
		}
		return transitionNo;
	}
		
	private void serializeGrafcetShell() {
		facG = GrafcetFactory.eINSTANCE;
		facT = TermsFactory.eINSTANCE;
		grafcet = facG.createGrafcet();
		grafcet.setVariableDeclarationContainer(facG.createVariableDeclarationContainer());
		createVariableDeclaration("testDummy", facT.createInteger(), VariableDeclarationType.INTERNAL);
	}
	public Grafcet getGrafcet() {
		return grafcet;
	}
	
	private void serializeInstance(GrafcetSerializerInstanceType instanceType) {
		switch (instanceType) {
		case BASIC_SEQUENCE: {
			serializeBasicSequence(m, createPartialgrafcet("G1"));
			
		} break;
		case BASIC_PARALLEL: {
			serializeBasicParallel(m, n);
			
		} break;
		case BASIC_VARIABLES: {
			serializeBasicVariables(m, n);
	
		} 
		break;
		case HIERARCHICAL_SEQUENCE: {
			serializeHierarchicalSequence(m, n);
	
		} 
		break;
		case HIERARCHICAL_PARALLEL: {
			serializeHierarchicalParallel(m, n);
	
		} 
		break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + instanceType);
		}
	}
	
	/**
	 * creates a simple loop with m steps in a sequence with one initial step and adds it to p 
	 * @param m no of steps
	 * @param p partial grafcet the loop is added to
	 */
	private void serializeBasicSequence(int m, PartialGrafcet p) {
		//PartialGrafcet p = createPartialgrafcet("G1");	
		
		Step s1 = createStep(true, getStepCount(), p);
		Transition t1 = createDefaultTransition(p);
		createArc(s1, t1, p);
		
		Transition tUpstream = t1;
		
		for (int i = 2; i <= m; i++) {
			Step s = createStep(false, getStepCount(), p);
			Transition t = createDefaultTransition(p);
			createArc(tUpstream, s, p);
			createArc(s, t, p);
			tUpstream = t;
		}
		
		createArc(tUpstream, s1, p);
	}
	
	/**
	 * Generates a partial grafcet with a recursive parallel structure of size: a0 = 3; a_i = a_i-1 - n^(i-1) + 3 * n^i
	 * automatic generation of ids only works for n < 10
	 * @param m depth of recusion
	 * @param n number of parallel sequences per recursion
	 */
	private void serializeBasicParallel(int m, int n) {
		PartialGrafcet p = createPartialgrafcet("G1");
		
		Step s1 = createStep(true, 1, p);
		Transition t1 = createDefaultTransition(p);
		createArc(s1, t1, p);
		
		Node middleNode;
		if (m > 0) {
			middleNode = recursionBasicParallel(t1, n, p, m, 1);
		} else if (m == 0) {
			middleNode = createStep(false, 11, p);
			createArc(t1, middleNode, p);
		} else {
			throw new IllegalArgumentException("Unexpected value for n: " + n);
		}
		
		
		Transition t2 = createDefaultTransition(p);
		createArc(middleNode, t2, p);
		Step s2 = createStep(false, 2, p);
		createArc(t2, s2, p);
		Transition t3 = createDefaultTransition(p);
		createArc(s2, t3, p);
		createArc(t3, s1, p);
//		System.out.println("Berechnung Reihe: " + getTransitionNo() + " - finaler transitionCount: " + (countTransition));
		
	}
	private Node recursionBasicParallel(Node topNode, int n, PartialGrafcet containingPG, int depth, int preId) {
		preId *= 10;
		Synchronization syncT = createSynchronization(containingPG);
		createArc(topNode, syncT, containingPG);
		Synchronization syncB = createSynchronization(containingPG);
		for (int j = 1; j <= n; j++) {
			Step sT = createStep(false, preId + j, containingPG);
			createArc(syncT, sT, containingPG);
			Transition tT = createDefaultTransition(containingPG);
			createArc(sT, tT, containingPG);
			
			Node middleNode;
			if (depth > 1) {
				middleNode = recursionBasicParallel(tT, n, containingPG, depth - 1, preId + j);
			} else {
				middleNode = createStep(false, (preId + j) * 10 + 1, containingPG);
				createArc(tT, middleNode, containingPG);
			}
			
			Transition tB = createDefaultTransition(containingPG);
			createArc(middleNode, tB, containingPG);
			Step sB = createStep(false, preId + j + n, containingPG);
			createArc(tB, sB, containingPG);
			createArc(sB, syncB, containingPG);
		}
		return syncB;
	}
	/**
	 * creates a partial grafcet with a simple loop of n steps. For every step it creates m actions for n different variables. 
	 * The initial step sets the variables to zero and all other n-1 steps incremet the m variables.
	 * n*m actions in total.
	 * @param m no of variable declarations
	 * @param n no of steps
	 */
	private void serializeBasicVariables(int m, int n) {
		PartialGrafcet p = createPartialgrafcet("G1");	
		serializeBasicSequence(n, p);
		
		for (int i = 1; i <= m; i++) {
			VariableDeclaration varDecl = createVariableDeclaration("k" + i, facT.createInteger(), VariableDeclarationType.INTERNAL);
			for(int j = 0; j < n; j++) {
				Step step = (Step) p.getSteps().get(j);
				if(step.isInitial()) {
					createActionAndActionLink(varDecl, step, p, createVariableZero());
				} else {
					createActionAndActionLink(varDecl, step, p, createIncrement(varDecl));
				}
			}
		}
	}
	
	/**
	 * creates m partial grafcets containing a simple loop of n steps with 1 initial step. n*m steps in total
	 * @param m no of partial Grafcets
	 * @param n no of steps per partial Grafcet
	 */
	private void serializeHierarchicalParallel(int m, int n) {
		for (int i = 1; i <= m; i++) {
			PartialGrafcet p = createPartialgrafcet("G" + i);
			serializeBasicSequence(n, p);
		}
	}
	
	
	private InitializableType createEnclosingStepLoop(PartialGrafcet containingPG, boolean hasEnclosingStep, 
			boolean hasInitialStep, boolean hasActivationLik, PartialGrafcet inferiorPartialGrafcet) {
		Step s1;
		if(hasActivationLik) {	
			s1 = createStepWActivationLink(hasInitialStep, getStepCount(), containingPG);
		} else {
			s1 = createStep(hasInitialStep, getStepCount(), containingPG);
		}
		Transition t1 = createDefaultTransition(containingPG);
		createArc(s1, t1, containingPG);
		
		Transition tUpstream = t1;
		
		for (int j = 2; j <= n - 1; j++) {
			Step s = createStep(false, getStepCount(), containingPG);
			Transition t = createDefaultTransition(containingPG);
			createArc(tUpstream, s, containingPG);
			createArc(s, t, containingPG);
			tUpstream = t;
		}
		
		InitializableType e;
		if(hasEnclosingStep) {
			if (inferiorPartialGrafcet == null ) {
				throw new IllegalArgumentException("inferiorpartialGrafcet == null");
			}
			e = createEnclosingStep(false, getStepCount(), containingPG, inferiorPartialGrafcet);
		} else {
			e = createStep(false , getStepCount(), containingPG);
		}
		createArc(tUpstream, e, containingPG);
		Transition tb = createDefaultTransition(containingPG);
		createArc(e, tb, containingPG);
		createArc(tb, s1, containingPG);
		
		return e;
	}
	/**
	 * creates m partial grafcets containing a loop of n steps. 
	 * Enclosing steps induce hierarchical dependencies between partial Grafcet forming a hierarchical branch.
	 * n * m steps;
	 * @param m no partial Grafcets
	 * @param n no steps per partial Grafcets
	 */
	private void serializeHierarchicalSequence(int m, int n) {
		if(n < 2) throw new IllegalArgumentException("n-values below 2 are not supported!");
		if(m < 2) throw new IllegalArgumentException("m-values below 2 are not supported!");
		PartialGrafcet pm = createPartialgrafcet("G" + m);
		createEnclosingStepLoop(pm, false, false, true, null); //most inferior
		PartialGrafcet inferior = pm;
		for (int i = m - 1; i > 1; i--) {
			PartialGrafcet p = createPartialgrafcet("G" + i);
			InitializableType e = createEnclosingStepLoop(p, true, false, true, inferior); // inbetween
			inferior.setEnclosingStep((EnclosingStep)e);
			inferior = p;
		}
		PartialGrafcet p1 = createPartialgrafcet("G" + 1);
		InitializableType e = createEnclosingStepLoop(p1, true, true, false, inferior); // most superior
		inferior.setEnclosingStep((EnclosingStep)e);
	}

	
	private ActionLink createActionAndActionLink(VariableDeclaration var, Step associatedStep, PartialGrafcet containingPG, Term value) {
		StoredAction a = facG.createStoredAction();
		a.setVariable(createVariable(var));
		a.setValue(value);
		
		containingPG.getActionTypes().add(a);
		ActionLink al = facG.createActionLink();
		al.setActionType(a);
		al.setStep(associatedStep);
		containingPG.getActionLinks().add(al);
		return al;
	}
	
	private Term createVariableZero() {
		IntegerConstant i = facT.createIntegerConstant();
		i.setValue(0);
		return i;
	}
	
	private Term createIncrement(VariableDeclaration varDecl) {
		Addition value = facT.createAddition();
		value.getSubterm().add(createVariable(varDecl));
		IntegerConstant i = facT.createIntegerConstant();
		i.setValue(1);
		value.getSubterm().add(i);
		return value;
	}
	
	private Term createNegatedVariable(VariableDeclaration vd, boolean isNegated) {
		Variable variable = createVariable(vd);
		if(isNegated) {
			Not not = facT.createNot();
			not.getSubterm().add(variable);
			return not;
		} else {
			return variable;
		}
	}
	
	private Variable createVariable(VariableDeclaration vd) {
		Variable v = facT.createVariable();
		v.setVariableDeclaration(vd);
		return v;
	}
	
	private VariableDeclaration createVariableDeclaration(String varName, Sort sort, VariableDeclarationType type) {
		VariableDeclaration varDecl = facT.createVariableDeclaration();
		varDecl.setName(varName);
		varDecl.setSort(sort);
		varDecl.setVariableDeclarationType(type);
		grafcet.getVariableDeclarationContainer().getVariableDeclarations().add(varDecl);
		return varDecl;
	}
	
	/**
	 * Creates step and adds it to the step list of containingPG.
	 * @param initial
	 * @param id
	 * @param containingPG
	 * @return
	 */
	private Step createStep(boolean initial, int id, PartialGrafcet containingPG) {
		Step s = facG.createStep();
		s.setInitial(initial);
		s.setId(id);
		containingPG.getSteps().add(s);
		return s;
	}
	
	private Step createStepWActivationLink(boolean initial, int id, PartialGrafcet containingPG) {
		Step s = createStep(initial, id, containingPG);
		s.setActivationLink(true);
		return s;
	}
	
	/**
	 * Creates transition this condition TRUE and adds it to the transition list of containingPG
	 * @param id
	 * @param containingPG
	 * @return
	 */
//	private Transition createTransitionTrue(int id, PartialGrafcet containingPG) {
//		return createTransitionFromTerm(id, containingPG, createTermTrue());
//	}
	
	private Transition createDefaultTransition(PartialGrafcet containingPG) {
		return createTransitionFromInputVar(containingPG);
//		return createTransitionTrue(count, containingPG);
	}
	
	private Transition createTransitionFromTerm(int id, PartialGrafcet containingPG, Term term) {
		Transition t = facG.createTransition();
		t.setId(id);
		containingPG.getTransitions().add(t);
		t.setTerm(term);
		return t;
	}
	
	private Transition createTransitionFromInputVar(PartialGrafcet containingPG) {
//		int size = inputVars.size();
//		List<VariableDeclaration> reminaingVars = new ArrayList<VariableDeclaration>(inputVars);
		
		Term root = null;
		And parentAnd = null;
		for (int i = 0; i < inputVars.size(); i++) {
			boolean isTrue = isVariableTrue(i, countTransition);
			Term var = createNegatedVariable(inputVars.get(i), isTrue);
			if (i < inputVars.size() - 1) {
				And and = facT.createAnd();
				and.getSubterm().add(var);
				if (parentAnd!= null) {
					parentAnd.getSubterm().add(and);
				} else {
					root = and;
				}
				parentAnd = and;
			} else {
				if (parentAnd != null) {
					parentAnd.getSubterm().add(var);
				} else {
					root = createVariable(inputVars.get(i));
				}
			}
		}

		Transition t = createTransitionFromTerm(countTransition + 1, containingPG, root);
		countTransition++;
		return t;
	}
	 
	private boolean isVariableTrue(int variableCount, int transitionCount) {
		String binaryString = Integer.toBinaryString(transitionCount);
//		System.out.println(binaryString);
		char[] c = binaryString.toCharArray();
		if(c.length <= variableCount) {
			return false;
		}
		if (c[c.length - variableCount - 1] == '1') {
//				System.out.println(c[variableCount] + " - " + true);
			return true;
		} else if (c[c.length - variableCount - 1] == '0') {
//				System.out.println(c[variableCount] + " - " + false);
			return false;
		} else {
			throw new IllegalArgumentException("Error in Algorithm");
		}
	}
	
	private EnclosingStep createEnclosingStep(boolean initial, int id, PartialGrafcet containingPG, PartialGrafcet inferiorPG) {
		EnclosingStep s = facG.createEnclosingStep();
		s.setInitial(initial);
		s.setId(id);
		containingPG.getSteps().add(s);
		s.getPartialGrafcets().add(inferiorPG);
		return s;
	}
	
	private Term createTermTrue() {
		BooleanConstant t = facT.createBooleanConstant();
		t.setValue(true);
		return t;
	}
	
	private PartialGrafcet createPartialgrafcet(String name) {
		PartialGrafcet p = facG.createPartialGrafcet();
		p.setName(name);
		grafcet.getPartialGrafcets().add(p);	
		return p;
	}
	
	private Arc createArc(Node source, Node target, PartialGrafcet containingPG) {
		Arc a = facG.createArc();
		a.setSource(source);
		a.setTarget(target);
		containingPG.getArcs().add(a);
		return a;
	}
	
	private Synchronization createSynchronization(PartialGrafcet containingPG) {
		Synchronization s = facG.createSynchronization();
		containingPG.getSynchronizations().add(s);
		return s;
	}

	


	

	public enum GrafcetSerializerInstanceType {
		BASIC_SEQUENCE, BASIC_PARALLEL, BASIC_VARIABLES, HIERARCHICAL_SEQUENCE, HIERARCHICAL_PARALLEL
	}
	
}
