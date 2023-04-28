package de.hsu.grafcet.instanceGeneration;

import de.hsu.grafcet.*;
import terms.Addition;
import terms.BooleanConstant;
import terms.IntegerConstant;
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
	
	/**
	 * @param instanceType type of instence to generate
	 * @param m item to iterate over
	 * @param n item to set a fixed size; might not be used
	 */
	public GrafcetSerializer(GrafcetSerializerInstanceType instanceType, int m, int n) {
		serializeGrafcetShell();
		this.n = n;
		this.m = m;
		serializeInstance(instanceType);

	}
		
	private void serializeGrafcetShell() {
		facG = GrafcetFactory.eINSTANCE;
		facT = TermsFactory.eINSTANCE;
		grafcet = facG.createGrafcet();
		grafcet.setVariableDeclarationContainer(facG.createVariableDeclarationContainer());
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
		
		int scale = (int) Math.pow(10, String.valueOf(m).length());
		
		Step s1 = createStep(true, scale + 1, p);
		Transition t1 = createTransition(scale + 1, p);
		createArc(s1, t1, p);
		
		Transition tUpstream = t1;
		
		for (int i = 2; i <= m; i++) {
			Step s = createStep(false, scale + i, p);
			Transition t = createTransition(scale + i, p);
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
		Transition t1 = createTransition(1, p);
		createArc(s1, t1, p);
		
		Node middleNode = recursionBasicParallel(t1, n, p, m, 1);
		
		
		Transition t2 = createTransition(2, p);
		createArc(middleNode, t2, p);
		Step s2 = createStep(false, 2, p);
		createArc(t2, s2, p);
		Transition t3 = createTransition(3, p);
		createArc(s2, t3, p);
		createArc(t3, s1, p);
		
	}
	private Node recursionBasicParallel(Node topNode, int n, PartialGrafcet containingPG, int depth, int preId) {
		preId *= 10;
		Synchronization syncT = createSynchronization(containingPG);
		createArc(topNode, syncT, containingPG);
		Synchronization syncB = createSynchronization(containingPG);
		for (int j = 1; j <= n; j++) {
			Step sT = createStep(false, preId + j, containingPG);
			createArc(syncT, sT, containingPG);
			Transition tT = createTransition(preId + j, containingPG);
			createArc(sT, tT, containingPG);
			
			Node middleNode;
			if (depth > 1) {
				middleNode = recursionBasicParallel(tT, n, containingPG, depth - 1, preId + j);
			} else {
				middleNode = createStep(false, (preId + j) * 10 + 1, containingPG);
				createArc(tT, middleNode, containingPG);
			}
			
			Transition tB = createTransition(preId + j + n, containingPG);
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
	private void serializeHierarchicalSequence(int m, int n) {
		for (int i = 1; i <= m; i++) {
			PartialGrafcet p = createPartialgrafcet("G" + i);
			serializeBasicSequence(n, p);
		}
	}
	
	
	private void createEnclosingStepLoop(PartialGrafcet containingPG, boolean hasEnclosingStep, boolean hasInitialStep, boolean hasActivationLik, int noSteps, PartialGrafcet inferiorPartialGrafcet) {
		Step s1;
		if(hasActivationLik) {	
			s1 = createStepWActivationLink(hasInitialStep, 1, containingPG);
		} else {
			s1 = createStep(hasInitialStep, 1, containingPG);
		}
		Transition t1 = createTransition(1, containingPG);
		createArc(s1, t1, containingPG);
		
		Transition tUpstream = t1;
		
		for (int j = 2; j <= m - 1; j++) {
			Step s = createStep(false, j, containingPG);
			Transition t = createTransition(j, containingPG);
			createArc(tUpstream, s, containingPG);
			createArc(s, t, containingPG);
			tUpstream = t;
		}
		
		InitializableType e;
		if(hasEnclosingStep) {
			if (inferiorPartialGrafcet == null ) {
				throw new IllegalArgumentException("inferiorpartialGrafcet == null");
			}
			e = createEnclosingStep(false, m, containingPG, inferiorPartialGrafcet);
		} else {
			e = createStep(false , m, containingPG);
		}
		createArc(tUpstream, e, containingPG);
		Transition tb = createTransition(1, containingPG);
		createArc(e, tb, containingPG);
		createArc(tb, s1, containingPG);
		
		
	}
	/**
	 * creates m partial grafcets containing a loop of n steps. 
	 * Enclosing steps induce hierarchical dependencies between partial Grafcet forming a hierarchical branch.
	 * n * m steps;
	 * @param m no partial Grafcets
	 * @param n no steps per partial Grafcets
	 */
	private void serializeHierarchicalParallel(int m, int n) {
		PartialGrafcet pm = createPartialgrafcet("G" + m);
		createEnclosingStepLoop(pm, false, false, true, n, null);
		PartialGrafcet inferior = pm;
		for (int i = m - 1; i > 1; i--) {
			PartialGrafcet p = createPartialgrafcet("G" + i);
			createEnclosingStepLoop(p, true, false, true, n, inferior);
			inferior = p;
		}
		PartialGrafcet p1 = createPartialgrafcet("G" + 1);
		createEnclosingStepLoop(p1, true, true, false, n, inferior);
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
	 * Creates transition and adds it to the transition list of containingPG
	 * @param id
	 * @param containingPG
	 * @return
	 */
	private Transition createTransition(int id, PartialGrafcet containingPG) {
		Transition t = facG.createTransition();
		t.setId(id);
		containingPG.getTransitions().add(t);
		
		t.setTerm(createTermTrue());
		
		return t;
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
