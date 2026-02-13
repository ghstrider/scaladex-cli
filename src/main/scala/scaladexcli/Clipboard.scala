package scaladexcli

import scala.sys.process.*

object Clipboard:

  def copy(text: String): Boolean =
    val os = sys.props.getOrElse("os.name", "").toLowerCase
    val cmd =
      if os.contains("mac") then Seq("pbcopy")
      else if os.contains("win") then Seq("clip.exe")
      else Seq("xclip", "-selection", "clipboard")

    try
      val proc = cmd.run(new ProcessIO(
        in => {
          in.write(text.getBytes("UTF-8"))
          in.close()
        },
        _ => (),
        _ => ()
      ))
      proc.exitValue() == 0
    catch
      case _: Exception =>
        if os.contains("linux") then tryXsel(text)
        else false

  private def tryXsel(text: String): Boolean =
    try
      val proc = Seq("xsel", "--clipboard", "--input").run(new ProcessIO(
        in => {
          in.write(text.getBytes("UTF-8"))
          in.close()
        },
        _ => (),
        _ => ()
      ))
      proc.exitValue() == 0
    catch
      case _: Exception => false
