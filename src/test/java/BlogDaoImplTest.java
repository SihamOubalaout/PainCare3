import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.JAVA.Beans.Blog;
import com.JAVA.DAO.DAOFactory;
import com.JAVA.DAO.BlogDaoImpl;

public class BlogDaoImplTest {

    private DAOFactory mockDAOFactory;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private BlogDaoImpl blogDAO;

    @Before
    public void setUp() throws SQLException {
        // Mock the DAOFactory and database-related objects
        mockDAOFactory = mock(DAOFactory.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Simulate the behavior of the getConnection method
        when(mockDAOFactory.getConnection()).thenReturn(mockConnection);

        // Mock the prepared statement to be returned when prepareStatement is called
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);

        // Initialize the BlogDaoImpl with the mocked DAOFactory
        blogDAO = new BlogDaoImpl(mockDAOFactory);
    }

    @Test
    public void testAddBlog() throws SQLException {
        // Define the blog object and set up the mock behavior for adding a blog
        Blog blog = new Blog();
        blog.setTitle("Test Blog");
        blog.setContent("This is a test blog content.");

        // Mock executeUpdate to simulate successful insert
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);  // Simulate successful insert
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);  // Mock ResultSet for generated keys
        when(mockResultSet.next()).thenReturn(true);  // Simulate that a key is returned
        when(mockResultSet.getInt(1)).thenReturn(1);  // Return the generated ID

        // Execute the DAO method
        boolean result = blogDAO.addBlog(blog);

        // Verify that the insert operation was called
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify the result of adding a blog
        assertTrue(result);  // Check if the blog was added successfully
    }

    @Test
    public void testGetBlogById() throws SQLException {
        // Mock the result set to simulate a blog retrieval
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Blog");
        when(mockResultSet.getString("content")).thenReturn("This is a test blog content.");

        // Mock the PreparedStatement to return the result set
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Execute the DAO method
        Blog blog = blogDAO.getBlogById(1);

        // Verify that the correct SQL query was executed
        verify(mockPreparedStatement, times(1)).executeQuery();

        // Verify the retrieved blog
        assertNotNull(blog);
        assertEquals(1, blog.getId());
        assertEquals("Test Blog", blog.getTitle());
        assertEquals("This is a test blog content.", blog.getContent());
    }

    @Test
    public void testUpdateBlog() throws SQLException {
        // Define the blog object to be updated
        Blog blog = new Blog();
        blog.setId(1);
        blog.setTitle("Updated Test Blog");
        blog.setContent("This is updated blog content.");

        // Mock executeUpdate to simulate successful update
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);  // Simulate successful update

        // Execute the DAO method
        boolean result = blogDAO.updateBlog(blog);

        // Verify that the update operation was called
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify the result of updating the blog
        assertTrue(result);  // Check if the blog was updated successfully
    }

    @Test
    public void testDeleteBlog() throws SQLException {
        // Mock executeUpdate to simulate successful deletion
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);  // Simulate successful delete

        // Execute the DAO method
        boolean result = blogDAO.deleteBlog(1);

        // Verify that the delete operation was called
        verify(mockPreparedStatement, times(1)).executeUpdate();

        // Verify the result of deleting the blog
        assertTrue(result);  // Check if the blog was deleted successfully
    }
}
