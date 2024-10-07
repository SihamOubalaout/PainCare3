import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.JAVA.Beans.Blog; // Assuming you have a Blog class in this package
import com.JAVA.DAO.DAOFactory;
import com.JAVA.DAO.BlogDAOImpl;

public class BlogDAOImplTest {

    private DAOFactory mockDAOFactory;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private BlogDAOImpl blogDAO;

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
        
        // Initialize the BlogDAOImpl with the mocked DAOFactory
        blogDAO = new BlogDAOImpl(mockDAOFactory);
    }

    @Test
    public void testInsertBlog() throws SQLException {
        // Define the blog object
        Blog blog = new Blog();
        blog.setTitle("Test Blog Title");
        blog.setContent("This is a test blog content.");
        blog.setAuthor("Test Author");

        // Mock executeUpdate behavior
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Simulate successful insert

        // Execute the DAO method
        blogDAO.insertBlog(blog);

        // Verify that the insert operation was called
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testGetBlogById() throws SQLException {
        // Mock the ResultSet behavior for getting a blog
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Blog Title");
        when(mockResultSet.getString("content")).thenReturn("This is a test blog content.");
        when(mockResultSet.getString("author")).thenReturn("Test Author");

        // Execute the DAO method
        Blog blog = blogDAO.getBlogById(1);

        // Verify the retrieved blog details
        assertNotNull(blog);
        assertEquals("Test Blog Title", blog.getTitle());
        assertEquals("This is a test blog content.", blog.getContent());
        assertEquals("Test Author", blog.getAuthor());
    }

    @Test
    public void testUpdateBlog() throws SQLException {
        // Define the blog object to update
        Blog blog = new Blog();
        blog.setId(1);
        blog.setTitle("Updated Blog Title");
        blog.setContent("Updated content for the blog.");
        blog.setAuthor("Updated Author");

        // Mock executeUpdate behavior
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Simulate successful update

        // Execute the DAO method
        blogDAO.updateBlog(blog);

        // Verify that the update operation was called
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteBlog() throws SQLException {
        // Mock executeUpdate behavior
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Simulate successful delete

        // Execute the DAO method
        blogDAO.deleteBlog(1);

        // Verify that the delete operation was called
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testGetAllBlogs() throws SQLException {
        // Mock the ResultSet behavior for getting all blogs
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false); // Simulate two blogs
        when(mockResultSet.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockResultSet.getString("title")).thenReturn("Blog 1 Title").thenReturn("Blog 2 Title");
        when(mockResultSet.getString("content")).thenReturn("Blog 1 Content").thenReturn("Blog 2 Content");
        when(mockResultSet.getString("author")).thenReturn("Author 1").thenReturn("Author 2");

        // Execute the DAO method
        List<Blog> blogs = blogDAO.getAllBlogs();

        // Verify the retrieved blogs
        assertNotNull(blogs);
        assertEquals(2, blogs.size());
        assertEquals("Blog 1 Title", blogs.get(0).getTitle());
        assertEquals("Blog 2 Title", blogs.get(1).getTitle());
    }
}
