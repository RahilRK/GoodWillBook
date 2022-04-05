package com.hksofttronix.goodwillbook.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Transaction.transactionModel;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class generatePDF {

    Context context;
    String TAG = this.getClass().getSimpleName();
    Globalclass globalclass;

    PdfWriter pdfWriter;

    public generatePDF(Context context) {
        this.context = context;
        globalclass = Globalclass.getInstance(context);
    }

    public String createPDF(String name, ArrayList<transactionModel> arrayList, String givereceive, String givereceiveamount, int sum)
    {
        String pdfpath = null;

        try
        {
            BaseFont app_font = null;
            try
            {
                app_font = BaseFont.createFont("assets/fonts/app_font.otf", "UTF-8", BaseFont.EMBEDDED);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //constructing the PDF file
            File pdfFile = new File(globalclass.getPdfpath());

            //Creating a Document with size A4. Document class is available at  com.itextpdf.text.Document
            Document document = new Document(PageSize.A4);
            document.addCreationDate();

            //assigning a PdfWriter instance to pdfWriter
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

            //PageFooter is an inner class of this class which is responsible to create Header and Footer
            PageHeaderFooter event = new PageHeaderFooter();
            pdfWriter.setPageEvent(event);

            //Before writing anything to a document it should be opened first
            document.open();

            //Adding Title(s) of the document
            addTitlePage(name,document,app_font,givereceive,givereceiveamount,sum);

            //Adding main contents of the document
            addContent(document,app_font,arrayList);

            pdfpath = pdfFile.getPath();
            document.close();
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,"createPDFException: "+error);
            globalclass.toast_short("Unable to generate pdf report!");
            return pdfpath;
        }

        return pdfpath;
    }

    void addTitlePage(String name, Document document, BaseFont app_font, String givereceive, String givereceiveamount, int sum)
            throws DocumentException {

        Paragraph paragraph = new Paragraph();

        Font FONT_1 = new Font(app_font, 16,Font.BOLD);
        Font FONT_2 = new Font(app_font, 14,Font.BOLD);
        Font FONT_3 = new Font(app_font, 14,Font.NORMAL);

        // Adding several title of the document. Paragraph class is available in  com.itextpdf.text.Paragraph
        Paragraph titleParagraph = new Paragraph(context.getResources().getString(R.string.application_name), FONT_1);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(titleParagraph);

        addEmptyLine(paragraph,1);

        Paragraph pName = new Paragraph();
        pName.add(new Chunk("Name: ",FONT_2));
        pName.add(new Chunk(name,FONT_3));
        paragraph.add(pName);

        addEmptyLine(paragraph,1);

        Paragraph pCreatedOn = new Paragraph();
        pCreatedOn.add(new Chunk("Created on: ",FONT_2));
        pCreatedOn.add(new Chunk(globalclass.formatDateTime_DBToUser(globalclass.getCurrentDatetime()),FONT_3));
        paragraph.add(pCreatedOn);

        addEmptyLine(paragraph,1);

        Paragraph pSummary = new Paragraph();
        pSummary.add(new Chunk(givereceive,FONT_2));
        Font FONT_For_RUPEE = FontFactory.getFont("assets/fonts/demo2.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        if(sum > 0)
        {
            FONT_For_RUPEE.setColor(BaseColor.GREEN);
        }
        else if(sum < 0)
        {
            FONT_For_RUPEE.setColor(BaseColor.RED);
        }
        else if(sum == 0)
        {
            FONT_For_RUPEE.setColor(BaseColor.BLACK);
        }
        pSummary.add(new Chunk(" "+givereceiveamount,FONT_For_RUPEE));
        paragraph.add(pSummary);

//        Paragraph datetimeParagraph = new Paragraph("Created on: "+globalclass.getCurrentDatetime(), FONT_SUBTITLE); //public static Font FONT_TITLE = new Font(Font.FontFamily.TIMES_ROMAN, 22,Font.BOLD);
//        datetimeParagraph.setAlignment(Element.ALIGN_LEFT);
//        paragraph.add(datetimeParagraph);

//        addEmptyLine(paragraph,1);
//
//        String FONT1 = "assets/fonts/demo2.ttf";
//        String RUPEE = "The Rupee character \u20B9 and the Rupee symbol \u20A8";
//        Font f1 = FontFactory.getFont(FONT1, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
//
//        Paragraph pDemo = new Paragraph();
//        pDemo.add(new Chunk(RUPEE, f1));
//        paragraph.add(pDemo);

        addEmptyLine(paragraph,2);


        document.add(paragraph);
    }

    void addContent(Document document, BaseFont app_font, ArrayList<transactionModel> arrayList) throws DocumentException {

        Font FONT_BODY = new Font(app_font, 12,Font.NORMAL);
        Paragraph reportBody = new Paragraph();
        reportBody.setFont(FONT_BODY); //public static Font FONT_BODY = new Font(Font.FontFamily.TIMES_ROMAN, 12,Font.NORMAL);

        // Creating a table
        createTable(reportBody,app_font,arrayList);

        // now add all this to the document
        document.add(reportBody);
    }

    void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    void createTable(Paragraph reportBody, BaseFont app_font, ArrayList<transactionModel> arrayList)
            throws BadElementException {

        float[] columnWidths = {2,5,2,5}; //total 4 columns and their width. The first three columns will take the same width and the fourth one will be 5/2.
        Font FONT_TABLE_HEADER = new Font(app_font, 12,Font.BOLD);
        PdfPTable table = new PdfPTable(columnWidths);

        table.setWidthPercentage(100); //set table with 100% (full page)
        table.getDefaultCell().setUseAscender(true);

        //Adding table headers
        PdfPCell cell;

        cell = new PdfPCell(new Phrase("SrNo", FONT_TABLE_HEADER));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); //alignment
//        cell.setBackgroundColor(new GrayColor(0.75f)); //cell background color
        cell.setBackgroundColor(new BaseColor(192, 192, 192,25)); //cell background color
        cell.setBorderColor(new BaseColor(192, 192, 192,25));
        cell.setFixedHeight(30); //cell height
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("DATETIME", FONT_TABLE_HEADER));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); //alignment
//        cell.setBackgroundColor(new GrayColor(0.75f)); //cell background color
        cell.setBackgroundColor(new BaseColor(192, 192, 192,25)); //cell background color
        cell.setBorderColor(new BaseColor(192, 192, 192,25));
        cell.setFixedHeight(30); //cell height
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("AMOUNT",
                FONT_TABLE_HEADER));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new GrayColor(0.75f));
        cell.setFixedHeight(30);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("STATUS",
                FONT_TABLE_HEADER));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new GrayColor(0.75f));
        cell.setFixedHeight(30);
        table.addCell(cell);


//        cell = new PdfPCell(new Phrase("IMAGE",
//                FONT_TABLE_HEADER));
//        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//        cell.setBackgroundColor(new GrayColor(0.75f));
//        cell.setFixedHeight(30);
//        table.addCell(cell);

        for(int i=0;i<arrayList.size();i++)
        {
            //datetime
            Font FONT_BLACK = new Font(app_font, 12,Font.BOLD);
            cell = new PdfPCell(new Phrase(String.valueOf(i+1),FONT_BLACK));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setFixedHeight(30);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(globalclass.formatDateTime_DBToUser(arrayList.get(i).getDebitdatetime()),FONT_BLACK));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setFixedHeight(30);
            table.addCell(cell);

            //amount
            Font FONT_For_RUPEE = FontFactory.getFont("assets/fonts/demo2.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
            if(globalclass.getuserid().equalsIgnoreCase(String.valueOf(arrayList.get(i).getDebituserid())))
            {

                FONT_For_RUPEE.setColor(BaseColor.RED);
                cell = new PdfPCell(new Phrase(String.valueOf("₹ ")+arrayList.get(i).getAmount(),FONT_For_RUPEE));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setFixedHeight(30);
                table.addCell(cell);
            }
            else
            {
                FONT_For_RUPEE.setColor(BaseColor.GREEN);
                cell = new PdfPCell(new Phrase(String.valueOf("₹ ")+arrayList.get(i).getAmount(),FONT_For_RUPEE));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setFixedHeight(30);
                table.addCell(cell);
            }


            //status
            if(arrayList.get(i).getIsApproved() == 1)
            {
                cell = new PdfPCell(new Phrase("Approved",FONT_BLACK));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setFixedHeight(30);
                table.addCell(cell);
            }
            else
            {
                Font FONT_Pending = new Font(app_font, 12,Font.BOLD);
                FONT_Pending.setColor(BaseColor.LIGHT_GRAY);
                cell = new PdfPCell(new Phrase("Pending",FONT_Pending));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setFixedHeight(30);
                table.addCell(cell);
            }

//            Image img = null;
//            try {
//                Drawable d = context.getResources().getDrawable(R.drawable.ic_approvedpng);
//                BitmapDrawable bitDw = ((BitmapDrawable) d);
//                Bitmap bmp = bitDw.getBitmap();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                img = Image.getInstance(stream.toByteArray());
//                img.scaleToFit(20, 20);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
////            img.setAbsolutePosition(10f,750f);
////            img.scaleToFit(50,50);
//            cell = new PdfPCell(img);
//            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            cell.setFixedHeight(30);
//            table.addCell(img);
        }

        reportBody.add(table);
    }

    class PageHeaderFooter extends PdfPageEventHelper {
        Font ffont = new Font(Font.FontFamily.UNDEFINED, 5, Font.ITALIC);

        public void onEndPage(PdfWriter writer, Document document) {

            Font FONT_HEADER_FOOTER = new Font(Font.FontFamily.UNDEFINED, 7, Font.ITALIC);

            /**
             * PdfContentByte is an object containing the user positioned text and graphic contents
             * of a page. It knows how to apply the proper font encoding.
             */
            PdfContentByte cb = writer.getDirectContent();

            /**
             * In iText a Phrase is a series of Chunks.
             * A chunk is the smallest significant part of text that can be added to a document.
             *  Most elements can be divided in one or more Chunks. A chunk is a String with a certain Font
             */
            Phrase footer_poweredBy = new Phrase("Powered By HK Softtronix", FONT_HEADER_FOOTER); //public static Font FONT_HEADER_FOOTER = new Font(Font.FontFamily.UNDEFINED, 7, Font.ITALIC);
            Phrase footer_pageNumber = new Phrase("Page " + document.getPageNumber(), FONT_HEADER_FOOTER);

            // Header
            // ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, header,
            // (document.getPageSize().getWidth()-10),
            // document.top() + 10, 0);

//			// footer: show page number in the bottom right corner of each age
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer_poweredBy, (document.right() - document.left()) / 2
                            + document.leftMargin(), document.bottom() - 10, 0);
            // footer: show page number in the bottom right corner of each age
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    footer_pageNumber,
                    (document.getPageSize().getWidth() - 10),
                    document.bottom() - 10, 0);
        }
    }

}
