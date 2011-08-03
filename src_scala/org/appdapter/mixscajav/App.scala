package org.appdapter.mixscajav

import org.appdapter.somejava._;

/**
 * @author ${user.name}
 */
object App {
  
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)
  
  def main(args : Array[String]) {
    println( "Helllllllllllllllo World!" )
    println("concat arguments = " + foo(args))
	JavaJavaJava.goJJJ();
  }

}
