package eventstore
package tcp

import EventStoreFormats._
import ReadDirection._
import akka.util.{ ByteStringBuilder, ByteIterator }
import scala.util.{ Success, Try }
import util.{ BytesWriter, BytesReader }

/**
 * @author Yaroslav Klymko
 */
object MarkerByte {
  type Reader = ByteIterator => Try[In]

  def readMessage(bi: ByteIterator): Reader = {
    val markerByte = bi.getByte
    readers.get(markerByte).getOrElse(sys.error(s"unknown marker byte: ${"0x%02X".format(markerByte)}"))
  }

  def reader[T <: In](implicit reader: BytesReader[T]): Reader = (bi: ByteIterator) => Success(reader.read(bi))

  def readerTry[T <: In](implicit reader: BytesReader[Try[T]]): Reader = reader.read

  val readers: Map[Byte, Reader] = Map[Int, Reader](
    0x01 -> reader[HeartbeatRequest.type],
    0x02 -> reader[HeartbeatResponse.type],
    0x03 -> reader[Ping.type],
    0x04 -> reader[Pong.type],

    //    PrepareAck = 0x05,
    //    CommitAck = 0x06,
    //
    //    SlaveAssignment = 0x07,
    //    CloneAssignment = 0x08,
    //
    //    SubscribeReplica = 0x10,
    //    CreateChunk = 0x11,
    //    PhysicalChunkBulk = 0x12,
    //    LogicalChunkBulk = 0x13,
    //
    0x83 -> readerTry[WriteEventsCompleted],
    0x85 -> readerTry[TransactionStartCompleted],
    0x87 -> readerTry[TransactionWriteCompleted],
    0x89 -> readerTry[TransactionCommitCompleted],
    0x8B -> readerTry[DeleteStreamCompleted.type],
    0xB1 -> readerTry[ReadEventCompleted],

    0xB3 -> readerTry[ReadStreamEventsCompleted](ReadStreamEventsForwardCompletedReader),
    0xB5 -> readerTry[ReadStreamEventsCompleted](ReadStreamEventsBackwardCompletedReader),

    0xB7 -> readerTry[ReadAllEventsCompleted](ReadAllEventsForwardCompletedReader),
    0xB9 -> readerTry[ReadAllEventsCompleted](ReadAllEventsBackwardCompletedReader),

    0xC1 -> reader[SubscribeCompleted],
    0xC2 -> reader[StreamEventAppeared],
    0xC4 -> readerTry[UnsubscribeCompleted.type],

    //    ScavengeDatabase = 0xD0,
    0xF0 -> reader[BadRequest.type] // TODO add test
    //    DeniedToRoute = 0xF1
    ).map {
      case (key, value) => key.toByte -> value
    }

  type Writer = ByteStringBuilder => Unit

  def writer[T <: Out](markerByte: Int, x: T)(implicit writer: BytesWriter[T]): (Writer, Writer) = {
    def writeByte(bb: ByteStringBuilder) {
      bb.putByte(markerByte.toByte)
    }
    def writeMessage(bb: ByteStringBuilder) {
      writer.write(x, bb)
    }
    (writeByte _) -> (writeMessage _)
  }

  def writeMessage(out: Out): (Writer, Writer) = out match {
    case x @ HeartbeatRequest  => writer(0x01, x)
    case x @ HeartbeatResponse => writer(0x02, x)
    case x @ Ping              => writer(0x03, x)
    case x @ Pong              => writer(0x04, x)
    //    PrepareAck = 0x05,
    //    CommitAck = 0x06,
    //
    //    SlaveAssignment = 0x07,
    //    CloneAssignment = 0x08,
    //
    //    SubscribeReplica = 0x10,
    //    CreateChunk = 0x11,
    //    PhysicalChunkBulk = 0x12,
    //    LogicalChunkBulk = 0x 13,
    case x: WriteEvents        => writer(0x82, x)
    case x: TransactionStart   => writer(0x84, x)
    case x: TransactionWrite   => writer(0x86, x)
    case x: TransactionCommit  => writer(0x88, x)
    case x: DeleteStream       => writer(0x8A, x)
    case x: ReadEvent          => writer(0xB0, x)
    case x: ReadStreamEvents => x.direction match {
      case Forward  => writer(0xB2, x)
      case Backward => writer(0xB4, x)
    }
    case x: ReadAllEvents => x.direction match {
      case Forward  => writer(0xB6, x)
      case Backward => writer(0xB8, x)
    }
    case x: SubscribeTo            => writer(0xC0, x)
    case x @ UnsubscribeFromStream => writer(0xC3, x)
    case x @ ScavengeDatabase      => writer(0xD0, x) // TODO
  }
}