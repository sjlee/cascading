/*
 * Copyright (c) 2007-2011 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package cascading.flow.planner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import cascading.flow.FlowElement;
import cascading.flow.Scope;
import cascading.tap.Tap;
import cascading.util.Util;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class StepGraph is an internal representation of {@link FlowStep} instances. */
public abstract class StepGraph extends SimpleDirectedGraph<FlowStep, Integer>
  {
  /** Field LOG */
  private static final Logger LOG = LoggerFactory.getLogger( StepGraph.class );

  /** Constructor StepGraph creates a new StepGraph instance. */
  public StepGraph()
    {
    super( Integer.class );
    }

  /**
   * Constructor StepGraph creates a new StepGraph instance.
   *
   * @param elementGraph of type ElementGraph
   * @param traps        of type Map<String, Tap>
   */
  public StepGraph( String flowName, ElementGraph elementGraph, Map<String, Tap> traps )
    {
    this();

    makeStepGraph( flowName, elementGraph, traps );

    verifyTrapsAreUnique( traps );
    }

  private void verifyTrapsAreUnique( Map<String, Tap> traps )
    {
    for( Tap tap : traps.values() )
      {
      if( Collections.frequency( traps.values(), tap ) != 1 )
        throw new PlannerException( "traps must be unique, cannot be reused on different branches: " + tap );
      }
    }

  /**
   * Method getCreateFlowStep ...
   *
   * @param flowName of type String
   * @param steps    of type Map<String, FlowStep>
   * @param sinkName of type String
   * @param numJobs  of type int
   * @return FlowStep
   */
  protected FlowStep getCreateFlowStep( String flowName, Map<String, FlowStep> steps, String sinkName, int numJobs )
    {
    if( steps.containsKey( sinkName ) )
      return steps.get( sinkName );

    LOG.debug( "creating step: {}", sinkName );

    String stepName = makeStepName( steps, numJobs, sinkName );
    int stepNum = steps.size() + 1;
    FlowStep step = createFlowStep( stepName, stepNum );

    step.setParentFlowName( flowName );

    steps.put( sinkName, step );

    return step;
    }

  protected abstract FlowStep createFlowStep( String stepName, int stepNum );

  private String makeStepName( Map<String, FlowStep> steps, int numJobs, String sinkPath )
    {
    // todo make the long form optional via a property
    if( sinkPath.length() > 75 )
      sinkPath = String.format( "...%75s", sinkPath.substring( sinkPath.length() - 75 ) );

    return String.format( "(%d/%d) %s", steps.size() + 1, numJobs, sinkPath );
    }

  protected abstract void makeStepGraph( String flowName, ElementGraph elementGraph, Map<String, Tap> traps );

  protected boolean pathContainsTap( GraphPath<FlowElement, Scope> path )
    {
    List<FlowElement> flowElements = Graphs.getPathVertexList( path );

    // first and last are taps, if we find more than 2, return false
    int count = 0;

    for( FlowElement flowElement : flowElements )
      {
      if( flowElement instanceof Tap )
        count++;
      }

    return count > 2;
    }

  public TopologicalOrderIterator<FlowStep, Integer> getTopologicalIterator()
    {
    return new TopologicalOrderIterator<FlowStep, Integer>( this, new PriorityQueue<FlowStep>( 10, new Comparator<FlowStep>()
    {
    @Override
    public int compare( FlowStep lhs, FlowStep rhs )
      {
      return Integer.valueOf( lhs.getSubmitPriority() ).compareTo( rhs.getSubmitPriority() );
      }
    } ) );
    }

  /**
   * Method writeDOT writes this element graph to a DOT file for easy visualization and debugging.
   *
   * @param filename of type String
   */
  public void writeDOT( String filename )
    {
    printElementGraph( filename );
    }

  protected void printElementGraph( String filename )
    {
    try
      {
      Writer writer = new FileWriter( filename );

      Util.writeDOT( writer, this, new IntegerNameProvider<FlowStep>(), new VertexNameProvider<FlowStep>()
        {
        public String getVertexName( FlowStep flowStep )
          {
          String sourceName = "";

          for( Object object : flowStep.getSources() )
            {
            Tap source = (Tap) object;

            if( source.isTemporary() )
              continue;

            sourceName += "[" + source.getPath() + "]";
            }

          String sinkName = flowStep.getSink().isTemporary() ? "" : "[" + flowStep.getSink().getPath() + "]";

          String groupName = flowStep.getGroup() == null ? "" : flowStep.getGroup().getName();

          String name = "[" + flowStep.getName() + "]";

          if( sourceName.length() != 0 )
            name += "\\nsrc:" + sourceName;

          if( groupName.length() != 0 )
            name += "\\ngrp:" + groupName;

          if( sinkName.length() != 0 )
            name += "\\nsnk:" + sinkName;

          return name.replaceAll( "\"", "\'" );
          }
        }, null );

      writer.close();
      }
    catch( IOException exception )
      {
      LOG.error( "failed printing graph to {}", filename, exception );
      }
    }

  }