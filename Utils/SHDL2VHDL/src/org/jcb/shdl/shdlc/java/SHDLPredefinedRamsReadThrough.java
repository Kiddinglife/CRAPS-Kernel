
package org.jcb.shdl.shdlc.java;

import java.util.*;
import java.util.regex.*;
import java.io.*;


public class SHDLPredefinedRamsReadThrough extends SHDLPredefinedOccurence {

	private int nbitAddr;
	private int nbitData;

	public SHDLPredefinedRamsReadThrough(SHDLModuleOccurence moduleOccurence, Pattern namePattern) {
		super(moduleOccurence, namePattern);
	}

	public boolean isInput(int index) {
		return (index <= 3);
	}
	public boolean isOutput(int index) {
		return (index > 3);
	}
	public boolean isInputOutput(int index) {
		return false;
	}
	public int getArity(int index) {
		switch (index) {
			case 0: return 1;
			case 1: return 1;
			case 2: return nbitAddr;
			case 3: return nbitData;
			case 4: return nbitData;
		}
		return -1;
	}

	public boolean check(boolean ok, SHDLModule module, PrintStream errorStream) {
		SHDLModuleOccurence moduleOccurence = getModuleOccurence();
		String name = moduleOccurence.getName().toLowerCase();
		Matcher m = getNamePattern().matcher(name);
		m.find();
		int nbadd = Integer.parseInt(m.group(1));
		String unitAddr = m.group(2);
		nbitData = Integer.parseInt(m.group(3));
		nbitAddr = log2(nbadd);
		if (nbitAddr == -1) {
			errorStream.println("** " + module.getFile().getName() + ":" + moduleOccurence.getBeginLine() + ": predefined RAM module '" + moduleOccurence.getName() + "' : the size must be a power of 2. Examples are: 'rams_read_through16Kx32', 'rams_read_through256x8', 'rams_read_through4Mx16'");
			return false;
		}
		if (unitAddr.equals("k"))
			nbitAddr += 10;
		else if (unitAddr.equals("m"))
			nbitAddr += 20;

		if (moduleOccurence.getArguments().size() != 5) {
			errorStream.println("** " + module.getFile().getName() + ":" + moduleOccurence.getBeginLine() + ": predefined RAM module '" + moduleOccurence.getName() + "' : it does not have the 5 mandatory arguments <clock>, <write>, <address>, <data_in>, <data_out>");
			return false;
		}
		SHDLSignal clkSignal = (SHDLSignal) moduleOccurence.getArguments().get(0);
		if (clkSignal.getArity() != 1) {
			errorStream.println("** " + module.getFile().getName() + ":" + moduleOccurence.getBeginLine() + ": predefined RAM module '" + moduleOccurence.getName() + "' : first argument (clock) must be a scalar");
			ok = false;
		}
		SHDLSignal weSignal = (SHDLSignal) moduleOccurence.getArguments().get(1);
		if (Math.abs(weSignal.getArity()) != 1) {
			errorStream.println("** " + module.getFile().getName() + ":" + moduleOccurence.getBeginLine() + ": predefined RAM module '" + moduleOccurence.getName() + "' : second argument (write enable) must be a scalar");
			ok = false;
		}
		SHDLSignal addrSignal = (SHDLSignal) moduleOccurence.getArguments().get(2);
		if (!checkArity(addrSignal, nbitAddr)) {
			errorStream.println("** " + module.getFile().getName() + ":" + moduleOccurence.getBeginLine() + ": predefined RAM module '" + moduleOccurence.getName() + "' : fourth argument (address) must have an arity of " + nbitAddr);
			ok = false;
		}
		SHDLSignal diSignal = (SHDLSignal) moduleOccurence.getArguments().get(3);
		if (!checkArity(diSignal, nbitData)) {
			errorStream.println("** " + module.getFile().getName() + ":" + moduleOccurence.getBeginLine() + ": predefined RAM module '" + moduleOccurence.getName() + "' : fifth argument (data in) must have an arity of " + nbitData);
			ok = false;
		}
		SHDLSignal doSignal = (SHDLSignal) moduleOccurence.getArguments().get(4);
		if (!checkArity(doSignal, nbitData)) {
			errorStream.println("** " + module.getFile().getName() + ":" + moduleOccurence.getBeginLine() + ": predefined RAM module '" + moduleOccurence.getName() + "' : sixth argument (data out) must have an arity of " + nbitData);
			ok = false;
		}
		checked = true;
		return ok;
	}

	public String getVHDLComponentDeclaration() {
		StringBuffer sb = new StringBuffer();
		sb.append("\tcomponent " + getModuleOccurence().getName() + SHDLModule.newline);
		sb.append("\t\tport (" + SHDLModule.newline);
		sb.append("\t\t\tclk  : in std_logic;" + SHDLModule.newline);
		sb.append("\t\t\twe  : in std_logic;" + SHDLModule.newline);
		sb.append("\t\t\taddr  : in std_logic_vector(" + (nbitAddr - 1) + " downto 0);" + SHDLModule.newline);
		sb.append("\t\t\tdi  : in std_logic_vector(" + (nbitData - 1) + " downto 0);" + SHDLModule.newline);
		sb.append("\t\t\tdo  : out std_logic_vector(" + (nbitData - 1) + " downto 0)" + SHDLModule.newline);
		sb.append("\t\t) ;" + SHDLModule.newline);
		sb.append("\tend component ;");
		return new String(sb);
	}

	public String getVHDLDefinition() {
		StringBuffer sb = new StringBuffer();
		sb.append("library ieee ;" + SHDLModule.newline);
		sb.append("use ieee.std_logic_1164.all ;" + SHDLModule.newline);
		sb.append("use ieee.std_logic_unsigned.all ;" + SHDLModule.newline);
		sb.append(SHDLModule.newline);
		sb.append("-- single-port RAM with synchronous read (read through)" + SHDLModule.newline);
		sb.append(SHDLModule.newline);
		sb.append("entity " + getModuleOccurence().getName() + " is" + SHDLModule.newline);
		sb.append("\tport (" + SHDLModule.newline);
		sb.append("\t\tclk  : in std_logic ;" + SHDLModule.newline);
		sb.append("\t\twe  : in std_logic ;" + SHDLModule.newline);
		sb.append("\t\taddr  : in std_logic_vector(" + (nbitAddr - 1) + " downto 0) ;" + SHDLModule.newline);
		sb.append("\t\tdi  : in std_logic_vector(" + (nbitData - 1) + " downto 0) ;" + SHDLModule.newline);
		sb.append("\t\tdo  : out std_logic_vector(" + (nbitData - 1) + " downto 0)" + SHDLModule.newline);
		sb.append("\t) ;" + SHDLModule.newline);
		sb.append("end " + getModuleOccurence().getName() + " ;" + SHDLModule.newline);
		sb.append(SHDLModule.newline);
		sb.append("architecture synthesis of " + getModuleOccurence().getName() + " is" + SHDLModule.newline);
		sb.append("\ttype ram_type is array (" + (((int) Math.pow(2, nbitAddr)) - 1) + " downto 0) of std_logic_vector (" + (nbitData - 1) + " downto 0) ;" + SHDLModule.newline);
		sb.append("\tsignal RAM: ram_type ;" + SHDLModule.newline);
		sb.append("\tsignal read_addr: std_logic_vector(" + (nbitAddr - 1) + " downto 0) ;" + SHDLModule.newline);
		sb.append(SHDLModule.newline);
		sb.append("begin" + SHDLModule.newline);
		sb.append("\tprocess (clk) begin" + SHDLModule.newline);
		sb.append("\t\tif clk'event and clk='1' then" + SHDLModule.newline);
		sb.append("\t\t\tif we='1' then" + SHDLModule.newline);
		sb.append("\t\t\t\tRAM(conv_integer(addr)) <= di ;" + SHDLModule.newline);
		sb.append("\t\t\tend if ;" + SHDLModule.newline);
		sb.append("\t\t\tread_addr <= addr ;" + SHDLModule.newline);
		sb.append("\t\tend if ;" + SHDLModule.newline);
		sb.append("\tend process ;" + SHDLModule.newline);
		sb.append("\tdo <= RAM(conv_integer(read_addr)) ;" + SHDLModule.newline);
		sb.append(SHDLModule.newline);
		sb.append("end synthesis ;" + SHDLModule.newline);
		return new String(sb);
	}

	int log2(int n) {
		if (n < 2) return -1;
		int res = 0;
		while (n >= 2) {
			if (n % 2 != 0) return -1;
			n = n / 2;
			res += 1;
		}
		return res;
	}

}

