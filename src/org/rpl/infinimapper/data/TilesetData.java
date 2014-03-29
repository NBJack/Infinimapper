package org.rpl.infinimapper.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

@DatabaseTable(tableName="tilelib")
public class TilesetData implements Identable<Integer> {

	public static final int DEFAULT_TILE_WIDTH = 32;
	public static final int DEFAULT_TILE_HEIGHT = 32;
    private static final int EMPTY_ID = -1;

    @DatabaseField(generatedId = true)
	private int id;
    @DatabaseField(columnName="fullwidth") private int width;
    @DatabaseField(columnName="fullheight") private int height;
    @DatabaseField private int border;
    @DatabaseField private int spacing;
    @DatabaseField private String name;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DatabaseField private String description;


    public TilesetData() {
        this.id = EMPTY_ID;
    }

	/**
	 * Grab a list of objects from a given result set.
	 * 
	 * @param dbFeed
	 * @return
	 * @throws SQLException
	 */
	public static Collection<TilesetData> grabFromStream(ResultSet dbFeed)
			throws SQLException {
		LinkedList<TilesetData> list = new LinkedList<TilesetData>();
		while (dbFeed.next()) {
			list.add(new TilesetData(dbFeed));
		}
		return list;
	}

	/**
	 * Construct an object from the database set.
	 * 
	 * @param dbFeed
	 * @throws SQLException
	 */
	public TilesetData(ResultSet dbFeed) throws SQLException {
		setId(dbFeed.getInt("id"));
		setWidth(dbFeed.getInt("fullwidth"));
		setHeight(dbFeed.getInt("fullheight"));
		setBorder(dbFeed.getInt("border"));
		setSpacing(dbFeed.getInt("spacing"));
	}

	private void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	private void setWidth(int width) {
		this.width = width;
	}

    /**
     * Gets the overall width of the image holding the tiles.
     * @return the image width.
     */
	public int getWidth() {
		return width;
	}

	private void setHeight(int height) {
		this.height = height;
	}

    /**
     * Gets the overall height of the image holding the tiles.
     * @return the image height.
     */
	public int getHeight() {
		return height;
	}

	private void setBorder(int border) {
		this.border = border;
	}

    /**
     * Gets the border in pixels surrounding the tiles.
     * @return
     */
	public int getBorder() {
		return border;
	}

	private void setSpacing(int spacing) {
		this.spacing = spacing;
	}

    /**
     * Gets the spacing between the tiles.
     * @return tile spacing.
     */
	public int getSpacing() {
		return spacing;
	}

    /**
     * Gets the uniform width of each tile.
     * @return each tile's width.
     */
	public int getTileWidth() {
		return DEFAULT_TILE_WIDTH;
	}

    /**
     * Gets the uniform height of each tile.
     * @return each tile's height.
     */
	public int getTileHeight() {
		return DEFAULT_TILE_HEIGHT;
	}

    /**
     * Gets the name of the tileset.
     * @return the tileset name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    /**
	 * Based on the given tile dimensions, calculate the total number of tiles
	 * in the set. This is a convenience function useful to help figure out GIDs
	 * in a set. The border and spacing are taken into account.
	 * 
	 * @return The total number of tiles in the set.
	 */
	public int calculateTileCount() {
		return ((width - 2 * getBorder()) / (getTileWidth() + getSpacing()))
				* ((height - 2 * getBorder()) / getTileHeight() + getSpacing());
	}

    @Override
    public Integer getID() {
        return id;
    }

    @Override
    public boolean hasId() {
        return id != EMPTY_ID;
    }


}
