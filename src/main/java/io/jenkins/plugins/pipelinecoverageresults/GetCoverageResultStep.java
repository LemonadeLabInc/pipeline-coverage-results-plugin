/*
    The MIT License
    Copyright (c) 2019 Richard Guenther

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

package io.jenkins.plugins.pipelinecoverageresults;

import hudson.PluginWrapper;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.coverage.CoverageAction;
import io.jenkins.plugins.coverage.targets.CoverageElement;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;

import static jenkins.model.Jenkins.getInstance;

import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashSet;
import java.util.Set;

public class GetCoverageResultStep extends Step {
  
    private static String element;

    @DataBoundConstructor
    public GetCoverageResultStep(String element) {
      this.element = element;
    }

    @Override
    public StepExecution start(StepContext context) {
      return new Execution(context, this.element);
    }
  
    @Extension
    public static class DescriptorImpl extends StepDescriptor {
  
      @Override
      public String getFunctionName() {
        return "getCoverageResult";
      }
  
      @Override
      public String getDisplayName() {
        return "Get coverage result";
      }

      @Override
      public Set<Class<?>> getRequiredContext() {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(TaskListener.class);
        return set;
      }
    }
  
    private static class Execution extends SynchronousStepExecution<Integer> {
    
      private int coverageValue = 0;
      private final String element;

      Execution(StepContext context, String element) {
        super(context);
        this.element = element;
      }

      @Override
      protected Integer run() throws Exception {

        // Check for Code Coverage API plugin
        PluginWrapper codeCoverageApiInstalled = getInstance().pluginManager.getPlugin("code-coverage-api");
        if (codeCoverageApiInstalled != null && codeCoverageApiInstalled.isActive()) {
          CoverageAction coverageAction = getContext().get(Run.class).getAction(CoverageAction.class);
          if (coverageAction != null) {
            CoverageElement coverageElement = CoverageElement.get(element);
            coverageValue = coverageAction.getResult().getCoverage(coverageElement).getPercentage();
          }
        }

        return coverageValue;
      }
    }
  }