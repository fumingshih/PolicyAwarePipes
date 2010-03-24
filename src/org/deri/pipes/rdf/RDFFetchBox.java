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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.deri.pipes.core.Context;
import org.deri.pipes.core.ExecBuffer;
import org.deri.pipes.core.PipeException;
import org.deri.pipes.model.BinaryContentBuffer;
import org.deri.pipes.model.SesameMemoryBuffer;
import org.deri.pipes.utils.HttpResponseCache;
import org.deri.pipes.utils.HttpResponseData;
import org.deri.pipes.utils.RDFlogger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
/**
 * @author Danh Le Phuoc, danh.lephuoc@deri.org
 *
 */
public class RDFFetchBox extends FetchBox {
	private transient Logger logger = LoggerFactory.getLogger(RDFFetchBox.class);
	@XStreamAsAttribute
	protected String format="RDF/XML";

	public ExecBuffer execute(Context context) throws Exception{
		SesameMemoryBuffer rdfBuffer=new SesameMemoryBuffer();
		HttpClient client= context.getHttpClient();
		RDFFormat fileFormat = getRDFFormat();
		Map<String,String> requestHeaders = new HashMap<String,String>();
		requestHeaders.put("Accept", fileFormat.getDefaultMIMEType());
		String url = location.expand(context);
		logger.debug("loading from "+url);
		try{
		HttpResponseData data = HttpResponseCache.getResponseData(client, url,requestHeaders);
		BinaryContentBuffer inputBuffer = data.toBinaryContentBuffer();
		rdfBuffer.load(inputBuffer.getInputStream(), url,fileFormat);
		/*
		 * A little hack here to insert the location in log with Type LOCATION as triple
		 */
		hackWithLocation(rdfBuffer,url);
		return rdfBuffer;
		}catch(Exception e){
			throw new PipeException("Could not load or parse "+url,e);
		}
	}

	private void hackWithLocation(SesameMemoryBuffer buffer,String url)throws RepositoryException{
		/*
		 * create a log with location as message into the repository
		 */
   	 	RepositoryConnection con = buffer.getConnection();
   	 	Repository rep = con.getRepository();
   	 	RDFlogger rdflogger = new RDFlogger(rep);

   	 try{
   		 System.out.println("rdffetch logging here!"+url);
   		rdflogger.log("LOCATION",url);

   	 }catch (Exception e){
			con.close();

   	 }
   	 finally{
			con.close();

   	 }

	}

	public void setFormat(String format) {
		this.format = format;
	}

	public RDFFormat getRDFFormat() {
		if(null==format){
    		logger.info("No format given, assuming rdfxml");
			return RDFFormat.RDFXML;
		}else{
			return(RDFFormat.valueOf(format));
		}
	}

}
