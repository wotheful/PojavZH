package com.oracle.dalvik;

public final class VMLauncher {
	private VMLauncher() {
	}
	@Keep @CriticalNative public static native int launchJVM(String[] args);

	static {
		System.loadLibrary("pojavexec");
	}
}
