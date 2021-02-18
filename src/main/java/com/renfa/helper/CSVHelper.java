package com.renfa.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.springframework.web.multipart.MultipartFile;

import com.renfa.exception.FileUploadContentException;
import com.renfa.model.User;

public class CSVHelper {
  public static String TYPE = "text/csv";
  static String[] HEADERS = { "id", "login", "name", "salary" };

  public static boolean hasCSVFormat(MultipartFile file) {

    if (!TYPE.equals(file.getContentType())) {
      return false;
    }

    return true;
  }

  public static List<User> csvToUsers(InputStream is) {
    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        CSVParser csvParser = new CSVParser(fileReader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

      List<User> users = new ArrayList<User>();

      Iterable<CSVRecord> csvRecords = csvParser.getRecords();

      for (CSVRecord csvRecord : csvRecords) {
        User user = new User(
              csvRecord.get(HEADERS[0]),
              csvRecord.get(HEADERS[1]),
              csvRecord.get(HEADERS[2]),
              Float.parseFloat(csvRecord.get(HEADERS[3]))
            );
        
        if(user.getSalary() < 0) {
          throw new FileUploadContentException(String.format("User %s has salary $.2f", user.getName(), user.getSalary()));
        }
        users.add(user);
      }

      return users;
    } catch (IOException e) {
      throw new RuntimeException("Fail to parse CSV file: " + e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException("Fail to parse CSV file: " + e.getMessage());
    }
  }

  public static ByteArrayInputStream usersToCSV(List<User> users) {
    final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);

    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {
      for (User user : users) {
        List<String> data = Arrays.asList(
              user.getId(),
              user.getLogin(),
              user.getName(),
              String.valueOf(user.getSalary())
            );

        csvPrinter.printRecord(data);
      }

      csvPrinter.flush();
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Fail to import data to CSV file: " + e.getMessage());
    }
  }

}