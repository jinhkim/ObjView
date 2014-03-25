package com.ubertome.objview;

import java.util.Scanner;

public class SystemPause {
	
	public SystemPause(){
		System.out.println("System Paused. Waiting for further input.\n(Press any key to continue)");
		new Scanner(System.in).nextLine();
	}

}
