/*
 * Copyright (c) 2021 Dmitry Barashev, BarD Software s.r.o.
 *
 * This file is part of GanttProject, an open-source project management tool.
 *
 * GanttProject is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * GanttProject is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */

package biz.ganttproject.print

import com.google.common.util.concurrent.AtomicDouble
import javafx.print.PageOrientation
import javafx.print.Printer
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import javax.imageio.ImageIO
import javax.print.attribute.standard.*
import kotlin.math.max
import javafx.print.PrinterJob as FxPrinterJob;
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import net.sourceforge.ganttproject.GPLogger
import java.awt.print.*
import javax.print.attribute.HashPrintRequestAttributeSet
import javafx.print.Paper as FxPaper

fun printPages(images: List<PrintPage>, mediaSize: MediaSize, orientation: Orientation) {
  val printJob = PrinterJob.getPrinterJob()
  printJob.setPageable(PageableImpl(images, mediaSize))
  val attr = HashPrintRequestAttributeSet().also {
    it.add(DialogTypeSelection.NATIVE)
    it.add(mediaSize.mediaSizeName)
    it.add(if (orientation == Orientation.LANDSCAPE) OrientationRequested.LANDSCAPE else OrientationRequested.PORTRAIT)
  }
  if (printJob.printDialog(attr)) {
    try {
      printJob.print(attr)
    } catch (e: Exception) {
      if (!GPLogger.log(e)) {
        e.printStackTrace(System.err)
      }
    }
  }
}
fun printPages(images: List<PrintPage>, paper: FxPaper) {
  val printJob = FxPrinterJob.createPrinterJob()
  printJob.jobSettings.pageLayout = printJob.printer.createPageLayout(
    paper, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM
  )
  if (printJob.showPrintDialog(null)) {
    images.forEach { page ->
      val image = Image(page.imageFile.inputStream())
      printJob.printPage(ImageView(image))
    }
  }
}

class PageableImpl(private val pages: List<PrintPage>, mediaSize: MediaSize) : Pageable {
  private val commonScale = AtomicDouble(0.0)
  private val pageFormat = createPageFormat(mediaSize)
  override fun getNumberOfPages() = pages.size
  override fun getPageFormat(pageIndex: Int) = pageFormat
  override fun getPrintable(pageIndex: Int) = PrintableImpl(pages[pageIndex], commonScale)
}

class PrintableImpl(private val page: PrintPage, private val commonScale: AtomicDouble) : Printable {
  override fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
    val image = ImageIO.read(page.imageFile)
    val g2d = graphics as? Graphics2D

    val scale = if (commonScale.get() == 0.0) {
      val imageWidthPt = image.width
      val imageHeightPt = image.height
      val scaleX = if (imageWidthPt < pageFormat.width) 1.0 else imageWidthPt/pageFormat.width
      val scaleY = if (imageHeightPt < pageFormat.height) 1.0 else imageHeightPt/pageFormat.height

      println("image width px=${image.width} pt=${imageWidthPt}")
      println("image height px=${image.height} pt=${imageHeightPt}")
      println("page width pt=${pageFormat.imageableWidth}")
      println("page height pt=${pageFormat.imageableHeight}")
      println("scaleX=$scaleX scaleY=$scaleY")

      1/(max(scaleX, scaleY))
    } else commonScale.get()
    println("scale=$scale")
    commonScale.set(scale)
    g2d?.setClip(
      0,
      0,
      image.width,
      image.height
    )

    val transform = AffineTransform.getScaleInstance(scale, scale)
    g2d?.drawRenderedImage(image, transform)

    return Printable.PAGE_EXISTS
  }

}
fun createPageFormat(mediaSize: MediaSize): PageFormat {
  return PageFormat().also { format ->
    //format.paper = Paper().also { paper ->
      //paper.setSize( mediaSize.getX(MediaSize.INCH) * 72.0, mediaSize.getY(MediaSize.INCH) * 72.0)
      //paper.setImageableArea(0.0, 0.0, paper.width, paper.height)
    //}
    //format.orientation = PageFormat.LANDSCAPE
  }
}

