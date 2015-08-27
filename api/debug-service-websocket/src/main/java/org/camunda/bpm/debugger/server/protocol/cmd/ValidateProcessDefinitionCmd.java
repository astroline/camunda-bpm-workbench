/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.debugger.server.protocol.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.debugger.server.protocol.dto.DeployProcessData;
import org.camunda.bpm.debugger.server.protocol.evt.ProcessDeployedEvt;
import org.camunda.bpm.debugger.server.protocol.evt.ProcessValidatedEvt;
import org.camunda.bpm.dev.debug.DebugSession;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import com.camunda.demo.bpmn_validation.BpmnValidationVisualizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Bernd Rücker
 *
 */
public class ValidateProcessDefinitionCmd extends DebugCommand<DeployProcessData> {

  public final static String NAME = "validate-process";

  public void execute(DebugCommandContext ctx) {

    String resourceName = data.getResourceName();
    String resourceData = data.getResourceData();

    final DebugSession debugSession = ctx.getDebugSession();
    ProcessEngine processEngine = debugSession.getProcessEngine();

    RepositoryService repositoryService = processEngine.getRepositoryService();

    Map<String, List<String>> errors = new HashMap<String, List<String>>();
    
    try { 
      String deploymentId = repositoryService.createDeployment()
          .addString(resourceName, resourceData)
          .deploy()
          .getId();
      
      repositoryService.deleteDeployment(deploymentId);
    }
    catch (ProcessEngineException ex) {      
      
      errors = BpmnValidationVisualizer.getErrors(ex);
      
      try {
      System.out.println(
          new ObjectMapper().writeValueAsString(errors));
      }
      catch (Exception ex2) {}
      
//      
//      errors.put("loadOrder", new ArrayList<String>());
//      errors.get("loadOrder").add("Kaputt");
//      errors.get("loadOrder").add("Und zwar richtig");
    }

    ctx.fireEvent(new ProcessValidatedEvt(errors));

  }

  public static void main(String[] args) throws JsonProcessingException {
    Map<String, List<String>> errors = new HashMap<String, List<String>>();
    
    errors.put("loadOrder", new ArrayList<String>());
    errors.get("loadOrder").add("Kaputt");
    errors.get("loadOrder").add("Und zwar richtig");
    
    errors.put("x", new ArrayList<String>());
    errors.get("x").add("Kaputt");
    errors.get("x").add("Und zwar richtig");

    System.out.println(
        new ObjectMapper().writeValueAsString(errors));
  }
  
}
