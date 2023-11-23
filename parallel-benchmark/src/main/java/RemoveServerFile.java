import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class RemoveServerFile {

	public static void main(String[] args) {
		Path serverLockFile = Path.of("serverLock.txt");
		try {
			Files.delete(serverLockFile);
		} catch (NoSuchFileException x) {
			System.err.format("%s: no such" + " file or directory%n", serverLockFile);
		} catch (DirectoryNotEmptyException x) {
			System.err.format("%s not empty%n", serverLockFile);
		} catch (IOException x) {
			// File permission problems are caught here.
			System.err.println(x);
		}
	}
}
