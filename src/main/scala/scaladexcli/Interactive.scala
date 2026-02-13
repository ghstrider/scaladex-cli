package scaladexcli

import scala.sys.process.*

object Interactive:

  private val Reset      = "\u001b[0m"
  private val Bold       = "\u001b[1m"
  private val Dim        = "\u001b[2m"
  private val Cyan       = "\u001b[36m"
  private val Green      = "\u001b[32m"
  private val ClearLine  = "\u001b[2K"
  private val HideCursor = "\u001b[?25l"
  private val ShowCursor = "\u001b[?25h"

  private enum Key:
    case Up, Down, Enter, Space, CtrlC, Other

  private def sttyGet(): String =
    Seq("sh", "-c", "stty -g < /dev/tty").!!.trim

  private def sttySet(args: String): Unit =
    Seq("sh", "-c", s"stty $args < /dev/tty").!

  private var ttyIn: java.io.FileInputStream = _

  private def withRawMode[A](f: => A): A =
    val saved = sttyGet()
    ttyIn = new java.io.FileInputStream("/dev/tty")
    try
      sttySet("-icanon -echo min 1")
      System.out.print(HideCursor)
      System.out.flush()
      f
    finally
      System.out.print(ShowCursor)
      System.out.flush()
      ttyIn.close()
      ttyIn = null
      sttySet(saved)

  private def readKey(): Key =
    val b = ttyIn.read()
    if b == -1 then Key.CtrlC
    else if b == 27 then
      val b2 = ttyIn.read()
      if b2 == 91 then
        ttyIn.read() match
          case 65 => Key.Up
          case 66 => Key.Down
          case _  => Key.Other
      else Key.Other
    else if b == 13 || b == 10 then Key.Enter
    else if b == 32 then Key.Space
    else if b == 3 then Key.CtrlC
    else Key.Other

  private def moveUp(n: Int): Unit =
    if n > 0 then System.out.print(s"\u001b[${n}A")

  private def renderSingle[A](
      items: List[A],
      display: A => String,
      cursor: Int
  ): Unit =
    items.zipWithIndex.foreach { case (item, i) =>
      System.out.print(s"$ClearLine\r")
      if i == cursor then
        System.out.println(s"  ${Cyan}>${Reset} ${Bold}${display(item)}${Reset}")
      else
        System.out.println(s"    ${display(item)}")
    }
    System.out.flush()

  private def renderMulti[A](
      items: List[A],
      display: A => String,
      cursor: Int,
      selected: Set[Int]
  ): Unit =
    items.zipWithIndex.foreach { case (item, i) =>
      System.out.print(s"$ClearLine\r")
      val marker =
        if selected.contains(i) then s"${Green}*${Reset}"
        else "o"
      if i == cursor then
        System.out.println(
          s"  ${Cyan}>${Reset} $marker ${Bold}${display(item)}${Reset}"
        )
      else
        System.out.println(s"    $marker ${display(item)}")
    }
    System.out.flush()

  def choose[A](prompt: String, items: List[A], display: A => String): A =
    items match
      case single :: Nil =>
        println(s"$prompt")
        println(s"  ${display(single)} ${Dim}(auto-selected)${Reset}")
        single
      case _ =>
        println(
          s"$prompt ${Dim}(arrow keys move, Enter selects)${Reset}"
        )
        withRawMode {
          var cursor = 0
          renderSingle(items, display, cursor)
          var done = false
          while !done do
            readKey() match
              case Key.Up =>
                cursor =
                  if cursor > 0 then cursor - 1 else items.size - 1
                moveUp(items.size)
                renderSingle(items, display, cursor)
              case Key.Down =>
                cursor =
                  if cursor < items.size - 1 then cursor + 1 else 0
                moveUp(items.size)
                renderSingle(items, display, cursor)
              case Key.Enter =>
                done = true
              case Key.CtrlC =>
                System.out.println()
                sys.exit(130)
              case _ => ()
          items(cursor)
        }

  def chooseMultiple[A](
      prompt: String,
      items: List[A],
      display: A => String
  ): List[A] =
    items match
      case single :: Nil =>
        println(s"$prompt")
        println(
          s"  ${Green}*${Reset} ${display(single)} ${Dim}(auto-selected)${Reset}"
        )
        items
      case _ =>
        println(
          s"$prompt ${Dim}(arrow keys move, Space toggles, Enter confirms)${Reset}"
        )
        withRawMode {
          var cursor = 0
          var selected = Set(0)
          renderMulti(items, display, cursor, selected)
          var done = false
          while !done do
            readKey() match
              case Key.Up =>
                cursor =
                  if cursor > 0 then cursor - 1 else items.size - 1
                moveUp(items.size)
                renderMulti(items, display, cursor, selected)
              case Key.Down =>
                cursor =
                  if cursor < items.size - 1 then cursor + 1 else 0
                moveUp(items.size)
                renderMulti(items, display, cursor, selected)
              case Key.Space =>
                selected =
                  if selected.contains(cursor) then selected - cursor
                  else selected + cursor
                moveUp(items.size)
                renderMulti(items, display, cursor, selected)
              case Key.Enter =>
                done = true
              case Key.CtrlC =>
                System.out.println()
                sys.exit(130)
              case _ => ()
          if selected.isEmpty then items
          else selected.toList.sorted.map(items(_))
        }
