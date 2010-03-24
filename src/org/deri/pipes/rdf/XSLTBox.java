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

import javax.xml.transform.stream.StreamSource;

import org.deri.pipes.core.ExecBuffer;
import org.deri.pipes.core.Context;
import org.deri.pipes.core.Operator;
import org.deri.pipes.core.internals.Source;
import org.deri.pipes.model.SesameTupleBuffer;
import org.deri.pipes.model.XMLStreamBuffer;
import org.deri.pipes.utils.XSLTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class XSLTBox implements Operator {
	private transient Logger logger = LoggerFactory.getLogger(XSLTBox.class);
	Source xmlsource;
	Source xslsource;
	
	@Override
	public ExecBuffer execute(Context context) throws Exception {
		XMLStreamBuffer buffer = new XMLStreamBuffer();
		boolean cancel = false;
		if(xmlsource==null){
			logger.warn("xmlsource is not set, cancelling operation");
			cancel = true;
		}
		if(xslsource==null){
			logger.warn("xslsource is not set, cancelling operation");
			cancel = true;
		}
		if(cancel){
			return buffer;
		}
		StreamSource xmlSrc=convert(xmlsource.execute(context));
		StreamSource xslSrc=convert(xslsource.execute(context));
		if((xmlSrc!=null)&&(xslSrc!=null)){
			buffer=new XMLStreamBuffer();	
			buffer.setStreamSource(XSLTUtil.transform(xmlSrc,xslSrc));				
		}
		return buffer;
	}
	
    private StreamSource convert(ExecBuffer xmlBuff) throws Exception{
		if(xmlBuff == null){
			return null;
		}
		return new StreamSource(xmlBuff.getInputStream());
    }


}