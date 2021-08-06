package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ProcessJUnitTestEx12 {
  
	 @Rule
	 @ClassRule
	  //public ProcessEngineRule rule = new ProcessEngineRule();
	 public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

	  @Before
	  public void setup() {
	    init(rule.getProcessEngine());
	  }
	  
  @Test
  @Deployment(resources = {"tweetApproval.dmn", "TwitterQAProcess_Ex12.bpmn"})
  public void testHappyPathEx12() {
    
	  Random rand = new Random();
	  // Create a HashMap to put in variables for the process instance
	    Map<String, Object> variables = new HashMap<String, Object>();
	    //variables.put("approved", false);
	    variables.put("content", "Exercise 12 test - Cheers "+rand.nextInt()+" !!!");
	    variables.put("email", "approved@camunda.com");
	   // variables.put("content", "Network error");
	    // Start process with Java API and variables
	    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcessEx12", variables);
	    
	    assertThat(processInstance).isWaitingAt(findId("Publish Tweet"));
	    
	    	    
	    // Handle Asynchronous Before save point
	    
	    List<Job> jobList = jobQuery()
	            .processInstanceId(processInstance.getId())
	            .list();
	        assertThat(jobList).hasSize(1);
	        Job job = jobList.get(0);
	        execute(job);
	        
	    // Make assertions on the process instance
	    assertThat(processInstance).isEnded();
  }
  
  @Test
  @Deployment(resources = {"tweetApproval.dmn", "TwitterQAProcess_Ex12.bpmn"})
  public void testNotHappyPathEx12() {
    
	  Random rand = new Random();
	  // Create a HashMap to put in variables for the process instance
	    Map<String, Object> variables = new HashMap<String, Object>();
	    //variables.put("approved", false);
	    variables.put("content", "Exercise 12 test - Cheers "+rand.nextInt()+" !!!");
	    variables.put("email", "rejected@camunda.com");
	    // Start process with Java API and variables
	    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcessEx12", variables);
	    
	   // assertThat(processInstance).isWaitingAt(findId("Send rejection notification"));
	   
	    assertThat(processInstance)
	     .isWaitingAt(findId("Send rejection notification"))
	     .externalTask()
	     .hasTopicName("notification");
	  complete(externalTask());
	  
	   assertThat(processInstance).isEnded().hasPassed(findId("Send rejection notification"));
 }

  

    
  @Test
  @Deployment(resources = "TwitterQAProcess_Ex12.bpmn")
  public void testSuperUserMessageEvents() {
	  
	  //start a process by creating a message and correlating it with an event.It will simply start the process
	 ProcessInstance processInstance = runtimeService()
		        .createMessageCorrelation("superuserTweetEx12")
		        .setVariable("content", "My Exercise 11 Tweet Cheers - " + System.currentTimeMillis())
		        .correlateWithResult()
		        .getProcessInstance();  
	 
	 assertThat(processInstance).isStarted();
	 
	 // get the job
	    List<Job> jobList = jobQuery()
	       .processInstanceId(processInstance.getId())
	       .list();

	    // execute the job
	    assertThat(jobList).hasSize(1);
	    Job job = jobList.get(0);
	    execute(job);

	    assertThat(processInstance).isEnded();
  }
  
  
  // Test DMN Table
  @Test
  @Deployment(resources = {"tweetApproval.dmn", "TwitterQAProcess_Ex12.bpmn"})
  public void testTweetFromDMN() {
   
	  Map<String, Object> variables5 = withVariables("email", "rejected@camunda.com", "content", "this should be rejected");
	  DmnDecisionTableResult decisionResult5 = decisionService().evaluateDecisionTableByKey("tweetApproval", variables5);
	  assertThat(decisionResult5.getFirstResult()).contains(entry("approved", false));
	  
	  Map<String, Object> variables = withVariables("email", "approved@camunda.com", "content", "this should be published");
	  DmnDecisionTableResult decisionResult = decisionService().evaluateDecisionTableByKey("tweetApproval", variables);
	  assertThat(decisionResult.getFirstResult()).contains(entry("approved", true));
	  
	  Map<String, Object> variables1 = withVariables("email", "rejected@camunda.com", "content", "this should be rejected");
	  DmnDecisionTableResult decisionResult1 = decisionService().evaluateDecisionTableByKey("tweetApproval", variables1);
	  assertThat(decisionResult1.getFirstResult()).contains(entry("approved", false));
	  
	  Map<String, Object> variables2 = withVariables("email", "rejected1@camunda.com", "content", "rocks this should be published");
	  DmnDecisionTableResult decisionResult2 = decisionService().evaluateDecisionTableByKey("tweetApproval", variables2);
	  assertThat(decisionResult2.getFirstResult()).contains(entry("approved", true));
	  
	  Map<String, Object> variables3 = withVariables("email", "approved1@camunda.com", "content", "rocks this should be published");
	  DmnDecisionTableResult decisionResult3 = decisionService().evaluateDecisionTableByKey("tweetApproval", variables3);
	  assertThat(decisionResult3.getFirstResult()).contains(entry("approved", true));
	  
	  Map<String, Object> variables4 = withVariables("email", "approved1@camunda.com", "content", "this should be published");
	  DmnDecisionTableResult decisionResult4 = decisionService().evaluateDecisionTableByKey("tweetApproval", variables4);
	  assertThat(decisionResult4.getFirstResult()).contains(entry("approved", false));
  }
}
