/*
 * Copyright (c) 2008-2009,
 * 
 * Digital Enterprise Research Institute, National University of Ireland, 
 * Galway, Ireland
 * http://www.deri.org/
 * http://pipes.deri.org/
 *
 * Semantic Web Pipes is distributed under New BSD License.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution and 
 *    reference to the source code.
 *  * The name of Digital Enterprise Research Institute, 
 *    National University of Ireland, Galway, Ireland; 
 *    may not be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.deri.pipes.rdf;

import org.deri.pipes.core.Context;
import org.deri.pipes.core.Engine;
import org.deri.pipes.core.ExecBuffer;
import org.deri.pipes.core.Pipe;
import org.deri.pipes.endpoints.PipeConfig;
import org.deri.pipes.text.TextBox;

import junit.framework.TestCase;

/**
 * @author robful
 *
 */
public class PipeCallBoxTest extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		Pipe pipe1 = new Pipe();
		TextBox x= new TextBox();
		x.setFormat(TextBox.TEXTPLAIN_FORMAT);
		x.setContent("hello ${word}");
		pipe1.addOperator(x);
		String syntax = Engine.defaultEngine().serialize(pipe1);
		PipeConfig config = new PipeConfig();
		config.setId("xxx");
		config.setSyntax(syntax);
		Engine.defaultEngine().getPipeStore().save(config);
	}

	public void test() throws Exception{		
		PipeCallBox pipeCall = new PipeCallBox();
		pipeCall.addParameter("word", "Giovanni Tummarello");
		pipeCall.setPipeId("xxx");
		Context context = Engine.defaultEngine().newContext();
		ExecBuffer result = pipeCall.execute(context);
		assertEquals("Wrong execute result","hello Giovanni Tummarello",result.toString());
	}

}
