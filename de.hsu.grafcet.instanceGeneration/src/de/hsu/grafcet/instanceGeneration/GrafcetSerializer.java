package de.hsu.grafcet.instanceGeneration;

import de.hsu.grafcet.*;
import terms.Addition;
import terms.BooleanConstant;
import terms.IntegerConstant;
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
			serializeBasicSequence(m);
			
		} break;
		case BASIC_PARALLEL: {
			serializeBasicParallel(m, n);
			
		} break;
		case BASIC_VARIABLES: {
			throw new IllegalArgumentException("Unexpected value: " + instanceType);
	
		} 
//		break;
		case HIERARCHICAL_SEQUENCE: {
			throw new IllegalArgumentException("Unexpected value: " + instanceType);
	
		} 
//		break;
		case HIERARCHICAL_PARALLEL: {
			throw new IllegalArgumentException("Unexpected value: " + instanceType);
	
		} 
//		break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + instanceType);
		}
	}
	
	/**
	 * creates a partial Grafcet with a simple loop with m steps in sequence
	 * @param m
	 */
	private void serializeBasicSequence(int m) {
		PartialGrafcet p = createPartialgrafcet("G1");	
		
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
	
	private void serializeBasicVariables() {
		PartialGrafcet p = facG.createPartialGrafcet();
		grafcet.getPartialGrafcets().add(p);
	}
	private void serializeHierarchicalSequence() {
		
	}
	private void serializeHierarchicalParallel() {
		
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
