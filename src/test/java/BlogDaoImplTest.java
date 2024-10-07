import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    private PreparedStatement mockPreparedStatementSelect;
    private PreparedStatement mockPreparedStatementUpdate;
    private PreparedStatement mockPreparedStatementDelete;
    private ResultSet mockResultSet;
    private ResultSet mockGeneratedKeys;
    private BlogDaoImpl blogDAO;

    @Before
    public void setUp() throws SQLException {
        // Initialize mocks
        mockDAOFactory = mock(DAOFactory.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockPreparedStatementSelect = mock(PreparedStatement.class);
        mockPreparedStatementUpdate = mock(PreparedStatement.class);
        mockPreparedStatementDelete = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockGeneratedKeys = mock(ResultSet.class);

        // Define behavior for DAOFactory
        when(mockDAOFactory.getConnection()).thenReturn(mockConnection);

        // Initialize BlogDaoImpl with mocked DAOFactory
        blogDAO = new BlogDaoImpl(mockDAOFactory);
    }

    // Test for addBlog - Success
    @Test
    public void testAddBlog_Success() throws SQLException, DAOException {
        // Arrange
        Blog blog = new Blog();
        blog.setTitle("JUnit Testing");
        blog.setDescription("Testing DAO layer with JUnit and Mockito.");
        blog.setPicture(new byte[]{1, 2, 3});
        blog.setPublicationDate(new Date());
        User user = new User();
        user.setId(1L);
        blog.setUser(user);

        // Mock PreparedStatement behavior for insert
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getLong(1)).thenReturn(100L);

        // Act
        blogDAO.addBlog(blog);

        // Assert
        verify(mockConnection).prepareStatement(
            "INSERT INTO blogs (title, description, picture, publicationDate, user_id) VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );

        verify(mockPreparedStatement).setString(1, "JUnit Testing");
        verify(mockPreparedStatement).setString(2, "Testing DAO layer with JUnit and Mockito.");
        verify(mockPreparedStatement).setBytes(3, new byte[]{1, 2, 3});
        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(mockPreparedStatement).setTimestamp(eq(4), timestampCaptor.capture());
        assertNotNull(timestampCaptor.getValue());
        verify(mockPreparedStatement).setLong(5, 1L);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();

        // Assert that blogId is set
        assertEquals(100L, blog.getBlogId());
    }

    // Test for addBlog - SQLException
    @Test(expected = DAOException.class)
    public void testAddBlog_SQLException() throws SQLException, DAOException {
        // Arrange
        Blog blog = new Blog();
        blog.setTitle("JUnit Testing");
        blog.setDescription("Testing DAO layer with JUnit and Mockito.");
        blog.setPicture(new byte[]{1, 2, 3});
        blog.setPublicationDate(new Date());
        User user = new User();
        user.setId(1L);
        blog.setUser(user);

        // Mock PreparedStatement to throw SQLException
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Insert failed"));

        // Act
        blogDAO.addBlog(blog);

        // Assert is handled by expected exception
    }

    // Test for getBlogById - Found
    @Test
    public void testGetBlogById_Found() throws SQLException, DAOException {
        // Arrange
        long blogId = 100L;

        Blog expectedBlog = new Blog();
        expectedBlog.setBlogId(blogId);
        expectedBlog.setTitle("JUnit Testing");
        expectedBlog.setDescription("Testing DAO layer with JUnit and Mockito.");
        expectedBlog.setPicture(new byte[]{1, 2, 3});
        expectedBlog.setPublicationDate(new Date());
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPassword("password");
        user.setPicture(new byte[]{4, 5, 6});
        user.setEmail("testuser@example.com");
        expectedBlog.setUser(user);

        // Mock PreparedStatement and ResultSet for select
        String sqlSelectById = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id " +
                "WHERE b.blog_id = ?";
        when(mockConnection.prepareStatement(sqlSelectById)).thenReturn(mockPreparedStatementSelect);
        when(mockPreparedStatementSelect.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("blog_id")).thenReturn(expectedBlog.getBlogId());
        when(mockResultSet.getString("title")).thenReturn(expectedBlog.getTitle());
        when(mockResultSet.getString("description")).thenReturn(expectedBlog.getDescription());
        when(mockResultSet.getBytes("picture")).thenReturn(expectedBlog.getPicture());
        when(mockResultSet.getTimestamp("publicationDate")).thenReturn(new Timestamp(expectedBlog.getPublicationDate().getTime()));
        when(mockResultSet.getLong("user_id")).thenReturn(user.getId());
        when(mockResultSet.getString("user_name")).thenReturn(user.getName());
        when(mockResultSet.getString("user_password")).thenReturn(user.getPassword());
        when(mockResultSet.getBytes("user_picture")).thenReturn(user.getPicture());
        when(mockResultSet.getString("user_email")).thenReturn(user.getEmail());

        // Act
        Blog actualBlog = blogDAO.getBlogById(blogId);

        // Assert
        verify(mockConnection).prepareStatement(sqlSelectById);
        verify(mockPreparedStatementSelect).setLong(1, blogId);
        verify(mockPreparedStatementSelect).executeQuery();

        assertNotNull(actualBlog);
        assertEquals(expectedBlog.getBlogId(), actualBlog.getBlogId());
        assertEquals(expectedBlog.getTitle(), actualBlog.getTitle());
        assertEquals(expectedBlog.getDescription(), actualBlog.getDescription());
        assertArrayEquals(expectedBlog.getPicture(), actualBlog.getPicture());
        assertEquals(expectedBlog.getPublicationDate().getTime(), actualBlog.getPublicationDate().getTime());
        assertNotNull(actualBlog.getUser());
        assertEquals(user.getId(), actualBlog.getUser().getId());
        assertEquals(user.getName(), actualBlog.getUser().getName());
        assertEquals(user.getPassword(), actualBlog.getUser().getPassword());
        assertArrayEquals(user.getPicture(), actualBlog.getUser().getPicture());
        assertEquals(user.getEmail(), actualBlog.getUser().getEmail());
    }

    // Test for getBlogById - Not Found
    @Test
    public void testGetBlogById_NotFound() throws SQLException, DAOException {
        // Arrange
        long blogId = 999L;

        // Mock PreparedStatement and ResultSet for select
        String sqlSelectById = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id " +
                "WHERE b.blog_id = ?";
        when(mockConnection.prepareStatement(sqlSelectById)).thenReturn(mockPreparedStatementSelect);
        when(mockPreparedStatementSelect.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No blog found

        // Act
        Blog actualBlog = blogDAO.getBlogById(blogId);

        // Assert
        verify(mockConnection).prepareStatement(sqlSelectById);
        verify(mockPreparedStatementSelect).setLong(1, blogId);
        verify(mockPreparedStatementSelect).executeQuery();

        assertNull(actualBlog);
    }

    // Test for updateBlog - Success
    @Test
    public void testUpdateBlog_Success() throws SQLException, DAOException {
        // Arrange
        Blog blog = new Blog();
        blog.setBlogId(100L);
        blog.setTitle("Updated Title");
        blog.setDescription("Updated Description");
        blog.setPicture(new byte[]{7, 8, 9});
        blog.setPublicationDate(new Date());
        User user = new User();
        user.setId(2L);
        blog.setUser(user);

        // Mock PreparedStatement for update
        String sqlUpdate = "UPDATE blogs SET user_id = ?, title = ?, description = ?, picture = ? WHERE blog_id = ?";
        when(mockConnection.prepareStatement(sqlUpdate)).thenReturn(mockPreparedStatementUpdate);
        when(mockPreparedStatementUpdate.executeUpdate()).thenReturn(1); // Simulate successful update

        // Act
        blogDAO.updateBlog(blog);

        // Assert
        verify(mockConnection).prepareStatement(sqlUpdate);
        verify(mockPreparedStatementUpdate).setLong(1, 2L);
        verify(mockPreparedStatementUpdate).setString(2, "Updated Title");
        verify(mockPreparedStatementUpdate).setString(3, "Updated Description");
        verify(mockPreparedStatementUpdate).setBytes(4, new byte[]{7, 8, 9});
        verify(mockPreparedStatementUpdate).setLong(5, 100L);
        verify(mockPreparedStatementUpdate).executeUpdate();
    }

    // Test for updateBlog - SQLException
    @Test(expected = DAOException.class)
    public void testUpdateBlog_SQLException() throws SQLException, DAOException {
        // Arrange
        Blog blog = new Blog();
        blog.setBlogId(100L);
        blog.setTitle("Updated Title");
        blog.setDescription("Updated Description");
        blog.setPicture(new byte[]{7, 8, 9});
        blog.setPublicationDate(new Date());
        User user = new User();
        user.setId(2L);
        blog.setUser(user);

        // Mock PreparedStatement to throw SQLException
        String sqlUpdate = "UPDATE blogs SET user_id = ?, title = ?, description = ?, picture = ? WHERE blog_id = ?";
        when(mockConnection.prepareStatement(sqlUpdate)).thenReturn(mockPreparedStatementUpdate);
        when(mockPreparedStatementUpdate.executeUpdate()).thenThrow(new SQLException("Update failed"));

        // Act
        blogDAO.updateBlog(blog);

        // Assert is handled by expected exception
    }

    // Test for deleteBlog - Success
    @Test
    public void testDeleteBlog_Success() throws SQLException, DAOException {
        // Arrange
        long blogId = 100L;

        // Mock PreparedStatement for delete
        String sqlDelete = "DELETE FROM blogs WHERE blog_id = ?";
        when(mockConnection.prepareStatement(sqlDelete)).thenReturn(mockPreparedStatementDelete);
        when(mockPreparedStatementDelete.executeUpdate()).thenReturn(1); // Simulate successful delete

        // Act
        blogDAO.deleteBlog(blogId);

        // Assert
        verify(mockConnection).prepareStatement(sqlDelete);
        verify(mockPreparedStatementDelete).setLong(1, blogId);
        verify(mockPreparedStatementDelete).executeUpdate();
    }

    // Test for deleteBlog - SQLException
    @Test(expected = DAOException.class)
    public void testDeleteBlog_SQLException() throws SQLException, DAOException {
        // Arrange
        long blogId = 100L;

        // Mock PreparedStatement to throw SQLException
        String sqlDelete = "DELETE FROM blogs WHERE blog_id = ?";
        when(mockConnection.prepareStatement(sqlDelete)).thenReturn(mockPreparedStatementDelete);
        when(mockPreparedStatementDelete.executeUpdate()).thenThrow(new SQLException("Delete failed"));

        // Act
        blogDAO.deleteBlog(blogId);

        // Assert is handled by expected exception
    }

    // Test for getBlogsByUserId - Success
    @Test
    public void testGetBlogsByUserId_Success() throws SQLException, DAOException {
        // Arrange
        long userId = 1L;

        Blog blog1 = new Blog();
        blog1.setBlogId(100L);
        blog1.setTitle("Blog One");
        blog1.setDescription("Description One");
        blog1.setPicture(new byte[]{1, 2, 3});
        blog1.setPublicationDate(new Date());
        User user = new User();
        user.setId(userId);
        blog1.setUser(user);

        Blog blog2 = new Blog();
        blog2.setBlogId(101L);
        blog2.setTitle("Blog Two");
        blog2.setDescription("Description Two");
        blog2.setPicture(new byte[]{4, 5, 6});
        blog2.setPublicationDate(new Date());
        blog2.setUser(user);

        List<Blog> expectedBlogs = Arrays.asList(blog1, blog2);

        // Mock PreparedStatement and ResultSet for getBlogsByUserId
        String sqlSelectByUserId = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id " +
                "WHERE b.user_id = ?";
        when(mockConnection.prepareStatement(sqlSelectByUserId)).thenReturn(mockPreparedStatementSelect);
        when(mockPreparedStatementSelect.executeQuery()).thenReturn(mockResultSet);

        // Mock ResultSet for two blogs
        when(mockResultSet.next()).thenReturn(true, true, false);
        // First blog
        when(mockResultSet.getLong("blog_id")).thenReturn(blog1.getBlogId());
        when(mockResultSet.getString("title")).thenReturn(blog1.getTitle());
        when(mockResultSet.getString("description")).thenReturn(blog1.getDescription());
        when(mockResultSet.getBytes("picture")).thenReturn(blog1.getPicture());
        when(mockResultSet.getTimestamp("publicationDate")).thenReturn(new Timestamp(blog1.getPublicationDate().getTime()));
        when(mockResultSet.getLong("user_id")).thenReturn(user.getId());
        when(mockResultSet.getString("user_name")).thenReturn(user.getName());
        when(mockResultSet.getString("user_password")).thenReturn(user.getPassword());
        when(mockResultSet.getBytes("user_picture")).thenReturn(user.getPicture());
        when(mockResultSet.getString("user_email")).thenReturn(user.getEmail());

        // Second blog
        when(mockResultSet.getLong("blog_id")).thenReturn(blog2.getBlogId());
        when(mockResultSet.getString("title")).thenReturn(blog2.getTitle());
        when(mockResultSet.getString("description")).thenReturn(blog2.getDescription());
        when(mockResultSet.getBytes("picture")).thenReturn(blog2.getPicture());
        when(mockResultSet.getTimestamp("publicationDate")).thenReturn(new Timestamp(blog2.getPublicationDate().getTime()));
        when(mockResultSet.getLong("user_id")).thenReturn(user.getId());
        when(mockResultSet.getString("user_name")).thenReturn(user.getName());
        when(mockResultSet.getString("user_password")).thenReturn(user.getPassword());
        when(mockResultSet.getBytes("user_picture")).thenReturn(user.getPicture());
        when(mockResultSet.getString("user_email")).thenReturn(user.getEmail());

        // Act
        List<Blog> actualBlogs = blogDAO.getBlogsByUserId(userId);

        // Assert
        verify(mockConnection).prepareStatement(sqlSelectByUserId);
        verify(mockPreparedStatementSelect).setLong(1, userId);
        verify(mockPreparedStatementSelect).executeQuery();

        assertNotNull(actualBlogs);
        assertEquals(2, actualBlogs.size());

        Blog actualBlog1 = actualBlogs.get(0);
        assertEquals(blog1.getBlogId(), actualBlog1.getBlogId());
        assertEquals(blog1.getTitle(), actualBlog1.getTitle());
        assertEquals(blog1.getDescription(), actualBlog1.getDescription());
        assertArrayEquals(blog1.getPicture(), actualBlog1.getPicture());
        assertEquals(blog1.getPublicationDate().getTime(), actualBlog1.getPublicationDate().getTime());
        assertNotNull(actualBlog1.getUser());
        assertEquals(user.getId(), actualBlog1.getUser().getId());

        Blog actualBlog2 = actualBlogs.get(1);
        assertEquals(blog2.getBlogId(), actualBlog2.getBlogId());
        assertEquals(blog2.getTitle(), actualBlog2.getTitle());
        assertEquals(blog2.getDescription(), actualBlog2.getDescription());
        assertArrayEquals(blog2.getPicture(), actualBlog2.getPicture());
        assertEquals(blog2.getPublicationDate().getTime(), actualBlog2.getPublicationDate().getTime());
        assertNotNull(actualBlog2.getUser());
        assertEquals(user.getId(), actualBlog2.getUser().getId());
    }

    // Test for searchBlogsByTitle - Success
    @Test
    public void testSearchBlogsByTitle_Success() throws SQLException, DAOException {
        // Arrange
        String titleSearch = "JUnit";

        Blog blog1 = new Blog();
        blog1.setBlogId(100L);
        blog1.setTitle("JUnit Testing");
        blog1.setDescription("Description One");
        blog1.setPicture(new byte[]{1, 2, 3});
        blog1.setPublicationDate(new Date());
        User user = new User();
        user.setId(1L);
        blog1.setUser(user);

        List<Blog> expectedBlogs = Arrays.asList(blog1);

        // Mock PreparedStatement and ResultSet for searchBlogsByTitle
        String sqlSearchByTitle = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id " +
                "WHERE b.title LIKE ?";
        when(mockConnection.prepareStatement(sqlSearchByTitle)).thenReturn(mockPreparedStatementSelect);
        when(mockPreparedStatementSelect.executeQuery()).thenReturn(mockResultSet);

        // Mock ResultSet for one blog
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getLong("blog_id")).thenReturn(blog1.getBlogId());
        when(mockResultSet.getString("title")).thenReturn(blog1.getTitle());
        when(mockResultSet.getString("description")).thenReturn(blog1.getDescription());
        when(mockResultSet.getBytes("picture")).thenReturn(blog1.getPicture());
        when(mockResultSet.getTimestamp("publicationDate")).thenReturn(new Timestamp(blog1.getPublicationDate().getTime()));
        when(mockResultSet.getLong("user_id")).thenReturn(user.getId());
        when(mockResultSet.getString("user_name")).thenReturn(user.getName());
        when(mockResultSet.getString("user_password")).thenReturn(user.getPassword());
        when(mockResultSet.getBytes("user_picture")).thenReturn(user.getPicture());
        when(mockResultSet.getString("user_email")).thenReturn(user.getEmail());

        // Act
        List<Blog> actualBlogs = blogDAO.searchBlogsByTitle(titleSearch);

        // Assert
        verify(mockConnection).prepareStatement(sqlSearchByTitle);
        verify(mockPreparedStatementSelect).setString(1, "%" + titleSearch + "%");
        verify(mockPreparedStatementSelect).executeQuery();

        assertNotNull(actualBlogs);
        assertEquals(1, actualBlogs.size());

        Blog actualBlog = actualBlogs.get(0);
        assertEquals(blog1.getBlogId(), actualBlog.getBlogId());
        assertEquals(blog1.getTitle(), actualBlog.getTitle());
        assertEquals(blog1.getDescription(), actualBlog.getDescription());
        assertArrayEquals(blog1.getPicture(), actualBlog.getPicture());
        assertEquals(blog1.getPublicationDate().getTime(), actualBlog.getPublicationDate().getTime());
        assertNotNull(actualBlog.getUser());
        assertEquals(user.getId(), actualBlog.getUser().getId());
    }

    // Test for getAllBlogs - Success
    @Test
    public void testGetAllBlogs_Success() throws SQLException, DAOException {
        // Arrange
        Blog blog1 = new Blog();
        blog1.setBlogId(100L);
        blog1.setTitle("Blog One");
        blog1.setDescription("Description One");
        blog1.setPicture(new byte[]{1, 2, 3});
        blog1.setPublicationDate(new Date());
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User One");
        user1.setPassword("password1");
        user1.setPicture(new byte[]{4, 5, 6});
        user1.setEmail("userone@example.com");
        blog1.setUser(user1);

        Blog blog2 = new Blog();
        blog2.setBlogId(101L);
        blog2.setTitle("Blog Two");
        blog2.setDescription("Description Two");
        blog2.setPicture(new byte[]{7, 8, 9});
        blog2.setPublicationDate(new Date());
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");
        user2.setPassword("password2");
        user2.setPicture(new byte[]{10, 11, 12});
        user2.setEmail("usertwo@example.com");
        blog2.setUser(user2);

        List<Blog> expectedBlogs = Arrays.asList(blog1, blog2);

        // Mock PreparedStatement and ResultSet for getAllBlogs
        String sqlSelectAll = "SELECT b.blog_id, b.title, b.description, b.picture, b.publicationDate, " +
                "u.id as user_id, u.name as user_name, u.password as user_password, u.picture as user_picture, u.email as user_email " +
                "FROM blogs b " +
                "INNER JOIN users u ON b.user_id = u.id";
        when(mockConnection.prepareStatement(sqlSelectAll)).thenReturn(mockPreparedStatementSelect);
        when(mockPreparedStatementSelect.executeQuery()).thenReturn(mockResultSet);

        // Mock ResultSet for two blogs
        when(mockResultSet.next()).thenReturn(true, true, false);

        // First blog
        when(mockResultSet.getLong("blog_id")).thenReturn(blog1.getBlogId());
        when(mockResultSet.getString("title")).thenReturn(blog1.getTitle());
        when(mockResultSet.getString("description")).thenReturn(blog1.getDescription());
        when(mockResultSet.getBytes("picture")).thenReturn(blog1.getPicture());
        when(mockResultSet.getTimestamp("publicationDate")).thenReturn(new Timestamp(blog1.getPublicationDate().getTime()));
        when(mockResultSet.getLong("user_id")).thenReturn(user1.getId());
        when(mockResultSet.getString("user_name")).thenReturn(user1.getName());
        when(mockResultSet.getString("user_password")).thenReturn(user1.getPassword());
        when(mockResultSet.getBytes("user_picture")).thenReturn(user1.getPicture());
        when(mockResultSet.getString("user_email")).thenReturn(user1.getEmail());

        // Second blog
        when(mockResultSet.getLong("blog_id")).thenReturn(blog2.getBlogId());
        when(mockResultSet.getString("title")).thenReturn(blog2.getTitle());
        when(mockResultSet.getString("description")).thenReturn(blog2.getDescription());
        when(mockResultSet.getBytes("picture")).thenReturn(blog2.getPicture());
        when(mockResultSet.getTimestamp("publicationDate")).thenReturn(new Timestamp(blog2.getPublicationDate().getTime()));
        when(mockResultSet.getLong("user_id")).thenReturn(user2.getId());
        when(mockResultSet.getString("user_name")).thenReturn(user2.getName());
        when(mockResultSet.getString("user_password")).thenReturn(user2.getPassword());
        when(mockResultSet.getBytes("user_picture")).thenReturn(user2.getPicture());
        when(mockResultSet.getString("user_email")).thenReturn(user2.getEmail());

        // Act
        List<Blog> actualBlogs = blogDAO.getAllBlogs();

        // Assert
        verify(mockConnection).prepareStatement(sqlSelectAll);
        verify(mockPreparedStatementSelect).executeQuery();

        assertNotNull(actualBlogs);
        assertEquals(2, actualBlogs.size());

        Blog actualBlog1 = actualBlogs.get(0);
        assertEquals(blog1.getBlogId(), actualBlog1.getBlogId());
        assertEquals(blog1.getTitle(), actualBlog1.getTitle());
        assertEquals(blog1.getDescription(), actualBlog1.getDescription());
        assertArrayEquals(blog1.getPicture(), actualBlog1.getPicture());
        assertEquals(blog1.getPublicationDate().getTime(), actualBlog1.getPublicationDate().getTime());
        assertNotNull(actualBlog1.getUser());
        assertEquals(user1.getId(), actualBlog1.getUser().getId());

        Blog actualBlog2 = actualBlogs.get(1);
        assertEquals(blog2.getBlogId(), actualBlog2.getBlogId());
        assertEquals(blog2.getTitle(), actualBlog2.getTitle());
        assertEquals(blog2.getDescription(), actualBlog2.getDescription());
        assertArrayEquals(blog2.getPicture(), actualBlog2.getPicture());
        assertEquals(blog2.getPublicationDate().getTime(), actualBlog2.getPublicationDate().getTime());
        assertNotNull(actualBlog2.getUser());
        assertEquals(user2.getId(), actualBlog2.getUser().getId());
    }
}
