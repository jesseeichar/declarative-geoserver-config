package c2c.geoserver

package object conf {
  def className[M](implicit m: Manifest[M]) = getClassName(m.erasure.getSimpleName)
  def toClassName(a:Any) = getClassName(a.getClass.getSimpleName)
  private def getClassName(className:String) = if (className.endsWith("$")) className.dropRight(1) else className
}