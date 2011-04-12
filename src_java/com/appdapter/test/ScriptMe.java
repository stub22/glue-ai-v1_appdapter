/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdapter.test;

/**
 *
 * @author Stu Baurmann
 */
public class ScriptMe {
	public static String yoYoYo(String arg) {
		return "she said [" + arg + "] - yo?";
	}
	public static void main(String[] args) {
		try {
			System.out.println("ScriptMe: " + yoYoYo("whatevs"));
		} catch(Throwable t) {
			System.out.println("Caught: " + t);
			t.printStackTrace();
		}
    }
}
