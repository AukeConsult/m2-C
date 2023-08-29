package no.auke.http;

public interface IResponseHandler
{
		void onComplete(byte[] data);
		void onError(int status);
}