package org.apache.linkis.engineconnplugin.seatunnel.executor

import org.apache.commons.lang.StringUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.conf.EngineConnConf.ENGINE_CONN_LOCAL_PATH_PWD_KEY
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.once.executor.{OnceExecutorExecutionContext, OperableOnceExecutor}
import org.apache.linkis.engineconnplugin.seatunnel.client.LinkisSeatunnelFlinkClient
import org.apache.linkis.engineconnplugin.seatunnel.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.seatunnel.config.SeatunnelFlinkEnvConfiguration.{LINKIS_FLINK_CHECK, LINKIS_FLINK_CONFIG, LINKIS_FLINK_RUNMODE, LINKIS_FLINK_VARIABLE}
import org.apache.linkis.engineconnplugin.seatunnel.config.SeatunnelEnvConfiguration
import org.apache.linkis.engineconnplugin.seatunnel.context.SeatunnelEngineConnContext
import org.apache.linkis.engineconnplugin.seatunnel.util.SeatunnelUtils.{generateExecFile, localArray}
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadInstanceResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse

import java.io.File
import java.nio.file.Files
import java.util
import java.util.concurrent.{Future, TimeUnit}

class SeatunnelFlinkOnceCodeExecutor(override val id: Long, override protected val seatunnelEngineConnContext: SeatunnelEngineConnContext) extends SeatunnelOnceExecutor with OperableOnceExecutor {
  private var params: util.Map[String, String] = _
  private var future: Future[_] = _
  private var daemonThread: Future[_] = _
  var isFailed = false

  override def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext, options: Map[String, String]): Unit = {
    val code: String = options(TaskConstant.CODE)
    params = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.asInstanceOf[util.Map[String, String]]
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        info("Try to execute codes."+code)
        if(runCode(code) !=0){
          isFailed = true
          setResponse(ErrorExecuteResponse("Run code failed!", new JobExecutionException("Exec Seatunnel Flink Code Error")))
          tryFailed()
        }
        info("All codes completed, now stop SeatunnelEngineConn.")
        closeDaemon()
        if(!isFailed) {
          trySucceed()
        }
        this synchronized notify()
      }
    })
  }

  protected def runCode(code: String):Int = {
    info("Execute SeatunnelFlink Process")

    var args:Array[String] =  Array.empty
    val flinkRunMode = LINKIS_FLINK_RUNMODE.getValue
    if(params != null && StringUtils.isNotBlank(params.get(flinkRunMode))) {
      val config = LINKIS_FLINK_CONFIG.getValue
      val variable = LINKIS_FLINK_VARIABLE.getValue
      val check = LINKIS_FLINK_CHECK.getValue

      args = Array(flinkRunMode,params.getOrDefault(flinkRunMode,"run"),
        check,params.getOrDefault(check,"false"),
        config,generateExecFile(code))

      if(params.containsKey(variable)) args ++(Array(variable,params.get(variable)))

    }else{
      args = localArray(code)
    }
    System.setProperty("SEATUNNEL_HOME",System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue));
    info(s"")
    Files.createSymbolicLink(new File(System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue)+"/seatunnel").toPath,new File(SeatunnelEnvConfiguration.SEATUNNEL_HOME.getValue).toPath)
    info(s"Execute SeatunnelFlink Process end args:${args.mkString(" ")}")
    LinkisSeatunnelFlinkClient.main(args)
  }


  override protected def waitToRunning(): Unit = {
    if (!isCompleted) daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        if (!(future.isDone || future.isCancelled)) {
          info("The Seatunnel Flink Process In Running")
        }
      }
    }, SeatunnelEnvConfiguration.SEATUNNEL_STATUS_FETCH_INTERVAL.getValue.toLong,
      SeatunnelEnvConfiguration.SEATUNNEL_STATUS_FETCH_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  }
  override def getCurrentNodeResource(): NodeResource = {
    val properties = EngineConnObject.getEngineCreationContext.getOptions
    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory = properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")) {
        properties.put(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key, settingClientMemory + "g")
      }
    }
    val actualUsedResource = new LoadInstanceResource(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.getValue(properties).toLong,
      EngineConnPluginConf.JAVA_ENGINE_REQUEST_CORES.getValue(properties), EngineConnPluginConf.JAVA_ENGINE_REQUEST_INSTANCE)
    val resource = new CommonNodeResource
    resource.setUsedResource(actualUsedResource)
    resource
  }

  protected def closeDaemon(): Unit = {
    if (daemonThread != null) daemonThread.cancel(true)
  }

  override def getProgress: Float = 0f

  override def getProgressInfo: Array[JobProgressInfo] = {
    Array.empty[JobProgressInfo]
  }


  override def getMetrics: util.Map[String, Any] = {
    new util.HashMap[String,Any]()
  }

  override def getDiagnosis: util.Map[String, Any] = new util.HashMap[String,Any]()
}
