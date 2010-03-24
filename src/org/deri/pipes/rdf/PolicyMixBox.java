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

import java.net.URL;

import org.deri.pipes.core.Context;
import org.deri.pipes.core.ExecBuffer;
import org.deri.pipes.core.Operator;
import org.deri.pipes.model.SesameMemoryBuffer;
import org.deri.pipes.utils.RDFlogger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author fuming
 * This class is to fetch the policy specified in two RDF resource and generate the merged result
 * It does two things,
 * 1. Check the policies of given resources if there is any conflict
 * 2. Add your policy to the
 */
public class PolicyMixBox extends AbstractMerge {

	private transient Logger logger = LoggerFactory.getLogger(PolicyMixBox.class);
    
	private RDFlogger rdflogger;
	/* (non-Javadoc)
	 * @see org.deri.pipes.core.Operator#execute(org.deri.pipes.core.Context)
	 *
	 */
	@Override
	public ExecBuffer execute(Context context) throws Exception {
   	 	SesameMemoryBuffer buffer= new SesameMemoryBuffer();
   	 	mergeInputs(buffer,context);
   	 	//merge with the content from previous context of RDFSMixBox
   	 	RepositoryConnection conn = buffer.getConnection();
   	 	Repository rep = conn.getRepository();

   	 	rdflogger = new RDFlogger(rep);


   	    /* now two rdf sources are merged together, prepare query the repository
   	 	 *
   	 	 */

     String policyURL = "http://foolme.csail.mit.edu/policy/myrdf.rdf";
     // Pipe can provide textBox and take "my_policy_url" from the box input
   	 URL url = new URL(policyURL);

   	PolicyMashup masher = new PolicyMashup(url);

   	masher.setRDFlogger(rep); //set the rdf logging place to the same current repos.

   	logger.info("policy pipe activated,policy pipe activated, policy pipe activated ");
   	rdflogger.log("INFO", "policy pipe activated,policy pipe activated, policy pipe activated");
	   	 try {
	   		 	masher.validateSource(rep);
				masher.validate(rep);
				logger.info("Validate, done!");
				rdflogger.log("POLICY","Validate, done!");

				//this check the policies of the given resources in the merged
				//log all the conflicts, could show in the UI later
				masher.merge(rep);
				logger.info("Merge my rss feed, done!");
				rdflogger.log("POLICY", "Merge my rss feed, done");
				//after validate and check, add its own policy to the repository
				// TODO:should be change to check the condition if the checking return true that it passes all the policies
				masher.addProvenance(rep);
				rdflogger.log("POLICY", "Add Provenance, done");

			} catch (RepositoryException e) {
				logger.warn("problem merging policy",e);
			}
	 	return buffer;


	}




}
