package amoeba

import com.typesafe.config.Config
import shapeless._
import scala.collection.JavaConverters._

class DelayedConfig(config: Config, path: String) {
  def getString = config.getString(path)
  def getDouble = config.getDouble(path)
  def getInt = config.getInt(path)
  def getConfigAt(newPath: String) = {
    if (newPath == "") this
    else if (path == "") new DelayedConfig(config, newPath)
    else new DelayedConfig(config.getConfig(path), newPath)
  }
  def isPresent = config.hasPath(path)
  def getList = config.getConfigList(path).asScala.toList.map(config => new DelayedConfig(config, ""))
}

trait ConfigReader[A] {
  def read(conf: DelayedConfig): A
}

object ConfigReader extends LabelledTypeClassCompanion[ConfigReader] {
  object typeClass extends LabelledTypeClass[ConfigReader] {
    override def coproduct[L, R <: Coproduct](name: String, headReader: => ConfigReader[L], tailReader: => ConfigReader[R]): ConfigReader[L :+: R] =
      config => if(config.getConfigAt("_type").getString == name) Inl(headReader.read(config)) else Inr(tailReader.read(config))

    override def emptyCoproduct: ConfigReader[CNil] =
      config => throw new RuntimeException("Impossible")

    override def product[H, T <: HList](name: String, headReader: ConfigReader[H], tailReader: ConfigReader[T]): ConfigReader[H :: T] =
      config => headReader.read(config.getConfigAt(name)) :: tailReader.read(config)

    override def emptyProduct: ConfigReader[HNil] =
      config => HNil

    override def project[F, G](instance: => ConfigReader[G], to: F => G, from: G => F): ConfigReader[F] =
      config => from(instance.read(config))
  }

  def instance[A](fn: DelayedConfig => A) = new ConfigReader[A] {
    override def read(conf: DelayedConfig): A = fn(conf)
  }

  implicit val stringReader = instance(_.getString)
  implicit val intReader = instance(_.getInt)
  implicit def optionalReader[A](implicit reader: ConfigReader[A]): ConfigReader[Option[A]] =
    instance(config => if(config.isPresent) Some(reader.read(config)) else None)
  implicit def listReader[A](implicit reader: ConfigReader[A]): ConfigReader[List[A]] =
    instance(config => if(config.isPresent) config.getList.map(conf => reader.read(conf)) else List.empty)

  def read[A](conf: Config, path: String)(implicit reader: ConfigReader[A]): A = reader.read(new DelayedConfig(conf, path))
}
