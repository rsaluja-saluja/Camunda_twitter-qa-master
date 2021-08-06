package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

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

public class ProcessJUnitTest {
  
	 @Rule
	 @ClassRule
	  //public ProcessEngineRule rule = new ProcessEngineRule();
	 public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

	  @Before
	  public void setup() {
	    init(rule.getProcessEngine());
	  }
	  
  @Test
  @Deployment(resources = "TwitterQAProcess.bpmn")
  public void testHappyPath() {
    
	  Random rand = new Random();
	  // Create a HashMap to put in variables for the process instance
	    Map<String, Object> variables = new HashMap<String, Object>();
	    //variables.put("approved", false);
	    variables.put("content", "Exercise 5 test - Cheers "+rand.nextInt()+" !!!");
	   // variables.put("content", "Network error");
	    // Start process with Java API and variables
	    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", variables);
	    
	    assertThat(processInstance).isWaitingAt(findId("Review Tweet"));
	    
	    List<Task> taskList = taskService()
	    	      				.createTaskQuery()
	    	      				.taskCandidateGroup("management")
	    	      				.processInstanceId(processInstance.getId())
	    	      				.list();
	    
	    assertThat(taskList).isNotNull();
	    assertThat(taskList).hasSize(1);
	    
	    Task task = taskList.get(0);
	    
	    Map<String, Object> approvedMap = new HashMap<String, Object>();
	    approvedMap.put("approved", true);
	    taskService().complete(task.getId(), approvedMap);
	    
	    // Handle Asynchronous Before save point
	    
	    List<Job> jobList = jobQuery()
	            .processInstanceId(processInstance.getId())
	            .list();
	        assertThat(jobList).hasSize(1);
	        Job job = jobList.get(0);
	        execute(job);
	        
	    // Make assertions on the process instance
	   // assertThat(processInstance).isEnded();
  }
  
  @Test
  @Deployment(resources = "TwitterQAProcess.bpmn")
  public void testNotHappyPath() {
    
	  Random rand = new Random();
	  // Create a HashMap to put in variables for the process instance
	    Map<String, Object> variables = new HashMap<String, Object>();
	    //variables.put("approved", false);
	    variables.put("content", "Exercise 5 test - Cheers "+rand.nextInt()+" !!!");
	    // Start process with Java API and variables
	    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", variables);
	    
	    assertThat(processInstance).isWaitingAt(findId("Review Tweet"));
	    
	    List<Task> taskList = taskService()
	    	      				.createTaskQuery()
	    	      				.taskCandidateGroup("management")
	    	      				.processInstanceId(processInstance.getId())
	    	      				.list();
	    
	    assertThat(taskList).isNotNull();
	    assertThat(taskList).hasSize(1);
	    
	    Task task = taskList.get(0);
	    
	    Map<String, Object> approvedMap = new HashMap<String, Object>();
	    approvedMap.put("approved", false);
	    taskService().complete(task.getId(), approvedMap);
	    
	    // Make assertions on the process instance
	   // assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "TwitterQAProcess.bpmn")
  public void testTweetRejected() {
	  Random rand = new Random();
	  
	  Map<String, Object> variables = new HashMap<String, Object>();
	  variables.put("approved", false);
	  variables.put("content", "Exercise 7 test - Cheers "+rand.nextInt()+" !!!");
	  ProcessInstance processInstance = runtimeService()
		        .createProcessInstanceByKey("TwitterQAProcess")
		        .setVariables(variables)
		        .startAfterActivity(findId("Review Tweet"))
		        .execute();
	  
	  assertThat(processInstance)
	     .isWaitingAt(findId("Send rejection notification"))
	     .externalTask()
	     .hasTopicName("notification");
	  complete(externalTask());
	  
	   assertThat(processInstance).isEnded().hasPassed(findId("Send rejection notification"));
  }
  
  @Test
  @Deployment(resources = "TwitterQAProcess.bpmn")
  public void testSuperUserMessageEvents() {
	  
	  //start a process by creating a message and correlating it with an event.It will simply start the process
	 ProcessInstance processInstance = runtimeService()
		        .createMessageCorrelation("superuserTweet")
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
  
  @Test
  @Deployment(resources="TwitterQAProcess.bpmn")
  public void testTweetWithdrawn() {
	  
	Map<String, Object> varMap = new HashMap<>();
    varMap.put("content", "Test tweetWithdrawn message");
    
    ProcessInstance processInstance = runtimeService()
        .startProcessInstanceByKey("TwitterQAProcess", varMap);
    
    assertThat(processInstance).isStarted().isWaitingAt(findId("Review Tweet"));
    
    runtimeService()
       .createMessageCorrelation("tweetWithdrawn")
       .processInstanceVariableEquals("content", "Test tweetWithdrawn message")
       .correlateWithResult();
    
    assertThat(processInstance).isEnded();
  }

  
}
