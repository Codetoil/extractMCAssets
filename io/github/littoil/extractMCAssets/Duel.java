package io.github.littoil.extractMCAssets;

import java.util.*;
import java.util.function.Predicate;

public class Duel<U, V> {
	public final U uObj;
	public final V vObj;

	Duel(U uObj, V vObj) {
		this.uObj = uObj;
		this.vObj = vObj;
	}

	@Override
	public String toString() {
		String uobjtxt = uObj.toString();
		String vobjtxt = vObj.toString();
		return "Duel (" + uobjtxt + "," + vobjtxt + ")";
	}


}
