package com.mobilemagic.scalainstaller

import android.content.Context
import android.util.Log
import java.io._

object RoboInstaller {
  def makeWritable() {
    Log.d(TAG, "Make /system writable")
    sudo("mount -o remount,rw /system")
  }

  def makeReadOnly() {
    Log.d(TAG, "Make /system read-only")
    sudo("mount -o remount,ro /system")
  }
  
  def sudo(cmd: String) {
    try {
      val proc: Process = runtime.exec(Array[String]("su", "-c", cmd))
      val res: Int = proc.waitFor
      if (res != 0) throw new RuntimeException("Execution of cmd '"+cmd+"' failed with exit code "+res)
    }
    catch {
      case e: Exception => {
        throw new RuntimeException(e)
      }
    }
  }

  final val TAG: String = "RoboInstaller"
  val resources = Array(R.raw.s1,
                        R.raw.s1_desc,
                        R.raw.s2,
                        R.raw.s2_desc,
                        R.raw.s3,
                        R.raw.s3_desc)

  val runtime: Runtime = Runtime.getRuntime
}

class RoboInstaller(ctx: Context) {

  private def performOperationOnSystem(op: => Unit) {
    try {
          RoboInstaller.makeWritable()
          op
        }
        catch {
          case e: Exception => {
            throw new RuntimeException(e)
          }
        }
        finally {
          RoboInstaller.makeReadOnly()
        }    
  }
  def installScalaLibs() {
    performOperationOnSystem {
      installFiles()
      makeLinks()
    }
  }
  
  def uninstallScalaLibs() {
    performOperationOnSystem {
      removeLinks()
    }
  }

  private def installFiles() {
    for (resid <- RoboInstaller.resources) installFile(resid)
  }

  private def makeLinks() {
    for{
      resid <- RoboInstaller.resources
      path = fileForResource(resid)
      if path.getName.endsWith("_desc.xml")
      newPath = s"/system/etc/permissions/${path.getName}"
    } {
      RoboInstaller.sudo(s"cp ${path.getAbsolutePath} $newPath")
      RoboInstaller.sudo(s"chmod 644 $newPath")
    }

  }

  private def removeLinks() {
    for (resid <- RoboInstaller.resources) {
      val path: File = fileForResource(resid)
      if (path.getName.endsWith("_desc.xml")) RoboInstaller.sudo("rm /system/etc/permissions/"+path.getName)
    }
  }

  /**
   * Takes the resource with the given name and installs it into the files dir
   * @param resid
   */
  private def installFile(resid: Int) {
    val targetFile: File = fileForResource(resid)
    Log.i(RoboInstaller.TAG, "Extracting file to " + targetFile.getAbsolutePath)
    val fos: FileOutputStream = new FileOutputStream(targetFile)
    val is: InputStream = ctx.getResources.openRawResource(resid)
    val buffer: Array[Byte] = new Array[Byte](65000)
    while (is.available > 0) {
      val read: Int = is.read(buffer)
      fos.write(buffer, 0, read)
    }
    is.close()
    fos.close()
    RoboInstaller.sudo("chmod 666 "+targetFile.getAbsolutePath)
  }

  private def fileForResource(resid: Int): File = {
    var namePart: String = lastPart(ctx.getResources.getResourceName(resid))
    if (namePart.endsWith("_desc")) namePart = namePart + ".xml"
    else namePart = namePart + ".jar"
    new File(ctx.getFilesDir, namePart)
  }

  private def lastPart(path: String): String = {
    path.substring(path.lastIndexOf("/") + 1)
  }
}

