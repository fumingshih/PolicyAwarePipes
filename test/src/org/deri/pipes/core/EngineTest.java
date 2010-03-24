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

package org.deri.pipes.core;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.deri.pipes.core.internals.Source;
import org.deri.pipes.endpoints.PipeConfig;
import org.deri.pipes.store.FilePipeStore;

/**
 * @author robful
 *
 */
public class EngineTest extends TestCase {
		private Source pipe2;
	public void test()throws Exception{
		Engine engine = new Engine();

		engine.setPipeStore(new FilePipeStore("data/pipe-library"));
		PipeConfig config = engine.getPipeStore().getPipe("test2");
		//PipeConfig config = engine.getPipeStore().getPipe("image");


//
//		System.out.println("testtest:"+ operatorXml);
//		DOMParser parser = new DOMParser();
//		String syntax = config.getSyntax();
// 		parser.parse(new InputSource(new java.io.StringReader(syntax)));
//
//

		Pipe pipe = (Pipe) engine.parse(config.getSyntax());
		ExecBuffer result = pipe.execute(engine.newContext());
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		result.stream(bout);
		System.out.println("output: "+bout.toString("UTF-8"));

		//Pipe pipe = engine.getPipe("tblotw");
	}

}