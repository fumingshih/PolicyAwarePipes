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

package org.deri.pipes.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;


/**
 * @author fuming
 * This is a temporary logger that create logging msg in RDF format, and add into repository too
 *
 */
public class RDFlogger {


	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd#HH-mm-ss_S";
	public static final String XML_DATETIME_FROMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String LOGDOMAIN = "http://foolme.csail.mit.edu/ns/policyawarelog#";
	public static final String PROVENANCE = "http://foolme.csail.mit.edu/ns/provenance#";

	private Calendar cal = Calendar.getInstance();

	//use sommer to convert to rdf
	private String classpath;
	private String function;
	private Repository repos;
	private ValueFactory f;


	// predicate URI
	private URI pal; //policy aware log
	private URI type;
	private URI timestamp;
	private URI message;
	private URI srcclass;
	private URI srcfunction;
	public RDFlogger(Repository rep){
		repos = rep;
		f = repos.getValueFactory();

		type = f.createURI(LOGDOMAIN,"type");
		timestamp = f.createURI(LOGDOMAIN,"timestamp");
		message = f.createURI(LOGDOMAIN,"message");
		srcclass = f.createURI(LOGDOMAIN,"srcclass");
		srcfunction = f.createURI(LOGDOMAIN, "srcfunction");
		pal = f.createURI(LOGDOMAIN, "log");
	}

	private String getdatetimeXSD(){
		//this method use an util class that parse java date to xml datetime, using SimpleDateFormat

		Calendar cal = Calendar.getInstance();
		DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
		return xdf.format(cal.getTime());

	}


	 private String now() {
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		    return sdf.format(cal.getTime());

	}

	public void log(String palType, String msg){

		/*
		 *  Logs will be saved as triple, the URI for log will be unique using id from datehash
		 *  Logging by making new connection to repository for every log could have performance issue
		 *  , but worry about this later
		 */
		String logid = now();
		URI log = f.createURI("http://foolme.csail.mit.edu/policy/log/log"+logid);

		Literal currenttime = f.createLiteral(getdatetimeXSD());
		Literal msgliteral = f.createLiteral(msg);
		Literal typeliteral = f.createLiteral(palType);
		String callingFunction =  Thread.currentThread().getStackTrace()[2].getMethodName();
		String callingClass =  Thread.currentThread().getStackTrace()[2].getClassName();
		Literal functionliteral = f.createLiteral(callingFunction);
		Literal classliteral = f.createLiteral(callingClass);

		try{
			 RepositoryConnection con = repos.getConnection();

			try{
				con.add(log, RDF.TYPE,pal);
				con.add(log, timestamp, currenttime);
				con.add(log, message, msgliteral);
				con.add(log, type,typeliteral);
				con.add(log, srcclass,classliteral);
				con.add(log, srcfunction,functionliteral);


			}finally{
				con.close();

			}
		 }catch(Exception e){}
	}








}
