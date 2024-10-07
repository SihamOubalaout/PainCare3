import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.JAVA.Beans.Blog;
import com.JAVA.Beans.User;
import com.JAVA.DAO.BlogDaoImpl;
import com.JAVA.DAO.DAOException;
import com.JAVA.DAO.DAOFactory;

public class BlogDaoImplTest {

    private DAOFactory mockDAOFactory;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private BlogDaoImpl blogDaoImpl;

    @Before
    public void setUp() throws Exception {
        // Initialize mocks
        mockDAOFactory = mock(DAOFactory.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Define behavior for DAOFactory
        when(mockDAOFactory.getConnection()).thenReturn(mockConnection);

        // Initialize BlogDaoImpl with mocked DAOFactory
        blogDaoImpl = new BlogDaoImpl(mockDAOFactory);
    }

    // Test for addBlog
    @Test
    public void testAddBlog_Success() throws Exception {
        // Arrange
        Blog blog = new Blog();
        blog.setTitle("JUnit Testing");
        blog.setDescription("Testing DAO layer with JUnit and Mockito.");
        blog.setPicture(new byte[]{1, 2, 3});
        blog.setPublicationDate(new Date());
        User user = new User();
        user.setId(1L); // Ensure user ID is long
        blog.setUser(user);

        // Mock PreparedStatement behavior
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(100L); // Use getLong instead of getInt

        // Act
        blogDaoImpl.addBlog(blog);

        // Assert interactions
        verify(mockConnection).prepareStatement(
            "INSERT INTO blogs (title, description, picture, publicationDate, user_id) VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );

        verify(mockPreparedStatement).setString(1, "JUnit Testing");
        verify(mockPreparedStatement).setString(2, "Testing DAO layer with JUnit and Mockito.");
        verify(mockPreparedStatement).setBytes(3, new byte[]{1, 2, 3});
        
        // Verify setTimestamp
        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(mockPreparedStatement).setTimestamp(eq(4), timestampCaptor.capture());
        assertNotNull(timestampCaptor.getValue());
        
        verify(mockPreparedStatement).setLong(5, 1L);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();

        // Assert that blogId is set
        assertEquals(100L, blog.getBlogId()); // Use 100L to match long
    }

    // Test for getBlogById - Found
    @Test
    public void testGetBlogById_Found() throws Exception {
        long blogId = 100L; // Use long instead of int

        // Mock prepareStatement and executeQuery
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Mock ResultSet data
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("blog_id")).thenReturn(blogId); // Use getLong
        when(mockResultSet.getString("title")).thenReturn("JUnit Testing");
        when(mockResultSet.getString("description")).thenReturn("Testing DAO layer with JUnit and Mockito.");
        when(mockResultSet.getBytes("picture")).thenReturn(new byte[]{1, 2, 3});
        when(mockResultSet.getTimestamp("publicationDate")).thenReturn(new Timestamp(System.currentTimeMillis()));
        // Assuming user is set in the map method

        // Act
        Blog blog = blogDaoImpl.getBlogById(blogId);

        // Assert interactions
        verify(mockConnection).prepareStatement(
            "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
            "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
            "FROM blogs b " +
            "INNER JOIN users u ON b.user_id = u.id " +
            "WHERE b.blog_id = ?"
        );

        verify(mockPreparedStatement).setLong(1, blogId); // Use setLong
        verify(mockPreparedStatement).executeQuery();

        // Assert Blog object
        assertNotNull(blog);
        assertEquals(blogId, blog.getBlogId());
        assertEquals("JUnit Testing", blog.getTitle());
        assertEquals("Testing DAO layer with JUnit and Mockito.", blog.getDescription());
        assertArrayEquals(new byte[]{1, 2, 3}, blog.getPicture());
        assertNotNull(blog.getPublicationDate());
        assertNotNull(blog.getUser()); // User is set in the map method
    }

    // Test for getBlogById - Not Found
    @Test
    public void testGetBlogById_NotFound() throws Exception {
        long blogId = 999L; // Use long instead of int

        // Mock prepareStatement and executeQuery
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Mock ResultSet to have no data
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Blog blog = blogDaoImpl.getBlogById(blogId);

        // Assert interactions
        verify(mockConnection).prepareStatement(
            "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
            "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
            "FROM blogs b " +
            "INNER JOIN users u ON b.user_id = u.id " +
            "WHERE b.blog_id = ?"
        );

        verify(mockPreparedStatement).setLong(1, blogId); // Use setLong
        verify(mockPreparedStatement).executeQuery();

        // Assert Blog object is null
        assertNull(blog);
    }

    // Test for deleteBlog - Success
    @Test
    public void testDeleteBlog_Success() throws Exception {
        long blogId = 100L; // Use long instead of int

        // Mock prepareStatement and executeUpdate
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // One row deleted

        // Act
        blogDaoImpl.deleteBlog(blogId);

        // Assert interactions
        verify(mockConnection).prepareStatement(
            "DELETE FROM blogs WHERE blog_id = ?"
        );

        verify(mockPreparedStatement).setLong(1, blogId); // Use setLong
        verify(mockPreparedStatement).executeUpdate();
    }

    // Test for deleteBlog - SQLException
    @Test(expected = DAOException.class)
    public void testDeleteBlog_SQLException() throws Exception {
        long blogId = 100L; // Use long instead of int

        // Mock prepareStatement to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Delete failed"));

        // Act & Assert
        blogDaoImpl.deleteBlog(blogId); // Expecting DAOException
    }

    // Additional tests for other methods can be added here
}
