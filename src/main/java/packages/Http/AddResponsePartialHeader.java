package packages.Http;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import enums.HttpStatus;

public class AddResponsePartialHeader {
    private final HttpExchange exchange;
    private final long fileSize;

    public AddResponsePartialHeader(HttpExchange exchange, long fileSize){
        this.exchange = exchange;
        this.fileSize = fileSize;
    }

    public void addPartialHeaderInfo(long currentSizeSent, long start, long end, String contentType, boolean isPartial)throws IOException{
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
        exchange.sendResponseHeaders(isPartial ? HttpStatus.PARTIAL_CONTENT.code : HttpStatus.OK.code, currentSizeSent);
    }

    public record RequestedBytesInfo(
        long start,
        long end
    ){}

    public RequestedBytesInfo parseBytesHeader(String bytesRangeRequestHeader) {
        if (bytesRangeRequestHeader == null) {
            return new RequestedBytesInfo(0, fileSize - 1);
        }

        //"bytes=0-999999"
        String[] rangeArr = bytesRangeRequestHeader.split("=");

        String[] startEnd = rangeArr[1].split("-");

        long start = Long.parseLong(startEnd[0]);
        
        long end = (startEnd.length < 2 || startEnd[1].isEmpty()) ? fileSize -1 : Long.parseLong(startEnd[1]);

        return new RequestedBytesInfo(start, end);
    }
}
