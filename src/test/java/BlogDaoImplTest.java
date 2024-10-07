import com.JAVA.Beans.Blog;
import com.JAVA.Beans.User;
import com.JAVA.DAO.BlogDAO;
import com.JAVA.DAO.BlogDaoImpl;
import com.JAVA.DAO.DAOException;
import com.JAVA.DAO.DAOFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BlogDaoImplTest {

    private DAOFactory mockDaoFactory;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private BlogDaoImpl blogDao;

    @Before
    public void setUp() throws Exception {
        // Set up mocks for DAOFactory, Connection, PreparedStatement, and ResultSet
        mockDaoFactory = Mockito.mock(DAOFactory.class);
        mockConnection = Mockito.mock(Connection.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockResultSet = Mockito.mock(ResultSet.class);

        // When getConnection() is called, return the mock connection
        when(mockDaoFactory.getConnection()).thenReturn(mockConnection);

        blogDao = new BlogDaoImpl(mockDaoFactory);
    }

    @Test
    public void testAddBlog() throws Exception {
        // Set up a blog object to be added
        Blog blog = new Blog();
        blog.setTitle("Test Title");
        blog.setDescription("Test Description");
        blog.setPicture(null);
        blog.setPublicationDate(new Date());

        User user = new User();
        user.setId(1); // Assuming the user with ID 1 exists
        blog.setUser(user);

        // Set up mock behavior for PreparedStatement and ResultSet
        when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1); // Returning generated blog ID 1

        // Test the addBlog method
        BlogDAO.addBlog(blog);

        // Verify the interactions with the mocks
        verify(mockPreparedStatement, times(1)).setString(1, blog.getTitle());
        verify(mockPreparedStatement, times(1)).setString(2, blog.getDescription());
        verify(mockPreparedStatement, times(1)).setBytes(3, blog.getPicture());
        verify(mockPreparedStatement, times(1)).setTimestamp(eq(4), any(Timestamp.class));
        verify(mockPreparedStatement, times(1)).setLong(5, blog.getUser().getId());
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockResultSet, times(1)).next();

        // Assert that the blog ID is set after the insert
        assertEquals(1, blog.getBlogId());
    }

    @Test(expected = DAOException.class)
    public void testAddBlogThrowsException() throws Exception {
        // Set up a blog object to be added
        Blog blog = new Blog();
        blog.setTitle("Test Title");
        blog.setDescription("Test Description");

        User user = new User();
        user.setId(1);
        blog.setUser(user);

        // Simulate SQLException being thrown
        when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException());

        // This should throw a DAOException
        blogDao.addBlog(blog);
    }
}
