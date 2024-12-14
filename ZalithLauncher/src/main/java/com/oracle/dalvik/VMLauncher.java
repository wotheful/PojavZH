package com.oracle.dalvik;

import androidx.annotation.Keep;
import dalvik.annotation.optimization.CriticalNative;

public final class VMLauncher {
	private VMLauncher() {
	}
	@Keep @CriticalNative public static native int launchJVM(String[] args);

	static {
		System.loadLibrary("pojavexec");
	}
}
