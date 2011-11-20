/*
 * Copyright 2011 The Yishishun Investment Management Co.,Ltd.
 * site: http://www.taobao.pk
 */
package tao.core.services

import java.util.concurrent.TimeUnit
import java.util.Date
import org.quartz.CronExpression;

import org.slf4j.LoggerFactory
import scala.actors.{ Actor, Exit, TIMEOUT }
/**
 * lightweight scheduler using scala reactWithin timeout pattern
 */
object Scheduler {
    trait Stopper{
        def stop
    }
    private val logger = LoggerFactory.getLogger(getClass)

    //scheuler start mode
    abstract class SchedulerStartMode

    //start at once
    case object StartAtOnce extends SchedulerStartMode
    //start at delay
    case object StartAtDelay extends SchedulerStartMode

    //scheduler
    def delayExecuteTask(f: => Unit, time: Long, unit: TimeUnit): Unit = {
        val timeOut = unit.toMillis(time);
        Actor.reactWithin(timeOut) {
            case TIMEOUT =>
                //execute function
                try { f } catch { case e: Throwable => logger.warn(e.toString, e) };
            case _ =>
                logger.warn("receive other some messsage")
        }
    }

    //schedule do some task
    // start mode as StartAtOnce or StartAtdelay
    def schedule(f: => Unit, exp: String, startMode: SchedulerStartMode=StartAtOnce) = new AnyRef with Stopper{
        import Actor._
        private val expression = new CronExpression(exp)
        private val a = actor {
            val waitingTime: Long = startMode match {
                //delay to start work
                case StartAtDelay =>
                    calculateNextTime(expression)
                case _ =>
                    //default one minute
                    10000
            }
            loop(waitingTime)
        }

        private def loop(time: Long): Unit = reactWithin(time) {
            case TIMEOUT =>
                //execute function
                try { f } catch { case e: Throwable => logger.warn(e.toString, e) };
                //loop next time
                loop(calculateNextTime(expression))
            case Exit =>
                reply();
                exit()
        }

        /**
         * stop the scheduler
         */

        override def stop() {
            logger.debug("stop scheduler")
            a !? Exit
        }
    }
    private def calculateNextTime(expression: CronExpression): Long = {
        val now = new Date()
        val nextTime = expression.getNextValidTimeAfter(now)
        nextTime.getTime - now.getTime
    }
}