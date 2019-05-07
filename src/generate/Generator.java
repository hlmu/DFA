package generate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.io.*;
import org.jgrapht.traverse.*;

public class Generator {
    public static final double EPS = 1e-6;
    Graph<Integer, DefaultWeightedEdge> fsmGraph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
    ComponentNameProvider<Integer> vertexIdProvider;
    ComponentNameProvider<Integer> vertexLabelProvider;
    ComponentNameProvider<DefaultWeightedEdge> edgeLabelProvider;
    ComponentAttributeProvider<Integer> vertexAttrProvider;
    ComponentAttributeProvider<DefaultWeightedEdge> edgeAttrProvider;
    int depth = 0;
    int cnt = 0;

    public static int sgn(double x, double y) {
        if (Math.abs(x - y) < EPS)
            return 0;
        else if (x < y)
            return -1;
        else
            return 1;
    }

    Generator(int depth) {
        this.depth = depth;
        this.cnt = (1 << (depth + 1)) - 1;
        int sum = (1 << depth) - 1; // sum of all nodes excluding the deepest level
        vertexIdProvider = new ComponentNameProvider<Integer>() {
            public String getName(Integer vertex) {
                return "q" + vertex;
            }
        };
        vertexLabelProvider = new ComponentNameProvider<Integer>() {
            public String getName(Integer vertex) {
                return "q" + vertex;
            }
        };
        vertexAttrProvider = new ComponentAttributeProvider<Integer>(){
            @Override
            public Map<String, Attribute> getComponentAttributes(Integer vertex) {
                //Attribute
                Map<String, Attribute> result = new HashMap<String, Attribute>();
                int val = vertex.intValue();
                if(val == Integer.MAX_VALUE) {
                    result.put("shape", DefaultAttribute.createAttribute("point"));
                }
                else if(val < sum) {
                    if((val&((1<<depth)-1)) != 0) {
                        result.put("shape", DefaultAttribute.createAttribute("doublecircle"));
                    }
                    else {
                        result.put("shape", DefaultAttribute.createAttribute("circle"));
                    }
                }
                else {
                    if(((val-sum)&((1<<depth)-1)) != 0) {
                        result.put("shape", DefaultAttribute.createAttribute("doublecircle"));
                    }
                    else {
                        result.put("shape", DefaultAttribute.createAttribute("circle"));
                    }
                }
                return result;
            }
        };
        edgeLabelProvider = new ComponentNameProvider<DefaultWeightedEdge>(){
            @Override
            public String getName(DefaultWeightedEdge edge) {
                double weight = fsmGraph.getEdgeWeight(edge);
                int v = fsmGraph.getEdgeTarget(edge);
                if(v == 0) return "start";
                if(sgn(weight, 0) == 0) return "0";
                else return "1";
            }
        };
        // edgeAttrProvider = new ComponentAttributeProvider<DefaultWeightedEdge>(){
        //     @Override
        //     public Map<String, Attribute> getComponentAttributes(DefaultWeightedEdge edge) {
        //         Map<String, Attribute> result = new HashMap<String, Attribute>();
        //         result.put("shape", DefaultAttribute.createAttribute("circle"));
        //         return result;
        //     }
        // };

        fsmGraph.addVertex(Integer.MAX_VALUE);
        // index of start point is 0
        for (int i = 0; i < this.cnt; i++) {
            fsmGraph.addVertex(i);
        }
        fsmGraph.addEdge(Integer.MAX_VALUE, 0); // start
        for (int i = 0; i < this.cnt / 2; i++) {
            DefaultWeightedEdge e = fsmGraph.addEdge(i, 2 * (i + 1) - 1);
            fsmGraph.setEdgeWeight(e, 0);
            e = fsmGraph.addEdge(i, 2 * (i + 1));
            fsmGraph.setEdgeWeight(e, 1);
        }
        for (int i = 0; i < (1 << depth); i++) {
            int source = i + sum;
            int sinkZero = ((i << 1) & ((1 << depth) - 1)) + sum;
            DefaultWeightedEdge e = fsmGraph.addEdge(source, sinkZero);
            fsmGraph.setEdgeWeight(e, 0);
            int sinkOne = (((i << 1) + 1) & ((1 << depth) - 1)) + sum;
            e = fsmGraph.addEdge(source, sinkOne);
            fsmGraph.setEdgeWeight(e, 1);
        }
    }

    public void exportToDot(String fileName) throws ExportException, IOException {
        GraphExporter<Integer, DefaultWeightedEdge> exporter = new DOTExporter<>(vertexIdProvider, vertexLabelProvider,
                edgeLabelProvider, vertexAttrProvider, null);
        Writer writer = new StringWriter();
        exporter.exportGraph(fsmGraph, writer);
        BufferedWriter bf = new BufferedWriter(new FileWriter(fileName));
        bf.write(writer.toString());
        bf.close();
    }

    public static void main(String[] args) throws ExportException, IOException {
        Generator generator = new Generator(10);
        generator.exportToDot("fsm.gv");
    }
}