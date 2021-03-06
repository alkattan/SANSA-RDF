package net.sansa_stack.rdf.spark.partition.core

import org.apache.jena.graph.Triple
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

import net.sansa_stack.rdf.partition.core.RdfPartition
import net.sansa_stack.rdf.partition.core.RdfPartitioner
import net.sansa_stack.rdf.partition.core.RdfPartitionerDefault

import scala.reflect.ClassTag


object RdfPartitionUtilsSpark extends Serializable {

  implicit def partitionGraph[P <: RdfPartition : ClassTag](graphRdd : RDD[Triple], partitioner: RdfPartitioner[P] = RdfPartitionerDefault) : Map[P, RDD[Row]] = {
    val map = Map(partitionGraphArray(graphRdd, partitioner) :_*)
    map
  }

  implicit def partitionGraphArray[P <: RdfPartition : ClassTag](graphRdd: RDD[Triple], partitioner: RdfPartitioner[P] = RdfPartitionerDefault) : Array[(P, RDD[Row])] = {
    val partitions = graphRdd.map(x => partitioner.fromTriple(x)).distinct.collect

    val array = partitions map { p => (
      p,
      graphRdd
        .filter(p.matches)
        .map(t => Row(p.layout.fromTriple(t).productIterator.toList :_* ))
        .persist())
    }

    array
  }
}
