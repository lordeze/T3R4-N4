package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.Slot;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.BindType;
import tera.gameserver.model.items.CrystalInstance;
import tera.gameserver.model.items.CrystalList;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, описывающий инвентарь игрока.
 *
 * @author Ronn Mod Evestu
 */
public class S_Inven extends ServerPacket
{
	private static final ServerPacket instance = new S_Inven();

	public static S_Inven getInstance(Player player)
	{
		S_Inven packet = (S_Inven) instance.newInstance();

		// получаем промежуточный буффер
		ByteBuffer buffer = packet.getPrepare();

		try
		{
			// получаем инвентарь
			Inventory inventory = player.getInventory();

			// получаем экиперовку
			Equipment equipment = player.getEquipment();

			// если чего-то нет, выходим
			if(inventory == null || equipment == null)
				return packet;

			inventory.lock();
			try
			{
				equipment.lock();
				try
				{
					// определяем кол-во всех ячеяк
					int total = inventory.getEngagedCells() + equipment.getEngagedSlots();

					packet.writeShort(buffer, total);
					packet.writeShort(buffer, 51);
					packet.writeInt(buffer, player.getObjectId());
					packet.writeInt(buffer, player.getSubId());
					packet.writeLong(buffer, inventory.getMoney());
					packet.writeByte(buffer, 0);// 0101
					packet.writeByte(buffer, 1);

					// получаем размер экиперовки
					int equipmentSize = equipment.size();

					// получаем размер инвенторя
					int inventorySize = inventory.getMaxCells();

					packet.writeByte(buffer, 0); // ignore byte
					packet.writeInt(buffer, inventorySize); // equipmentSize
					packet.writeInt(buffer, 0x1);
					packet.writeInt(buffer, 0x1);
					packet.writeInt(buffer, 0x0); // new
					packet.writeInt(buffer, 0x0); // new
					packet.writeInt(buffer, 0x0); // new
					// получаем все ячейки инвенторя
					Cell[] cells = inventory.getCells();

					// получаем все слоты экиперовки
					Slot[] slots = equipment.getSlots();

					int n = 0x33;

					// перебираем экиперовку
					for(int i = 0; i < equipmentSize; i++)
					{
						// получаем итем
						ItemInstance item = slots[i].getItem();

						// если его нет, пропускаем
						if(item == null)
							continue;

						packet.writeShort(buffer, n);

						n += 151;

						packet.writeShort(buffer, n);
						
						packet.writeInt(buffer, 0);
						packet.writeShort(buffer, n - 2);
						
						packet.writeInt(buffer, item.getItemId());
						packet.writeInt(buffer, item.getObjectId());
						packet.writeInt(buffer, 0);
						packet.writeInt(buffer, player.getObjectId());
						packet.writeInt(buffer, 0);
						packet.writeInt(buffer, i + 1);
						packet.writeInt(buffer, 0);
						packet.writeLong(buffer, item.getItemCount());
						//packet.writeInt(buffer, 0);
						packet.writeByte(buffer, 0x1E);// !!!

						// получаем кристалы итема
						CrystalList crystals = item.getCrystals();

						// если кристалов нет
						if(crystals == null || crystals.isEmpty())
						{
							packet.writeInt(buffer, 0); // 1 ячейка итема
							packet.writeInt(buffer, 0); // 2 ячейка итема
							packet.writeInt(buffer, 0); // 3 ячейка итема
							packet.writeInt(buffer, 0); // 4 ячейка итема
						}
						else
						{
							int diff = 4 - crystals.size();

							CrystalInstance[] array = crystals.getArray();

							// вносим вставленные кристалы
							for(int g = 0, length = crystals.size(); g < length; g++)
								packet.writeInt(buffer, array[g].getItemId());

							// если есть пустые слоты
							if(diff > 0)
								// указываем кол-во пустых слотов
								for(int g = 0; g < diff; g++)
									packet.writeInt(buffer, 0);
						}

						packet.writeByte(buffer, 0);
						packet.writeShort(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeShort(buffer, 0);
						packet.writeShort(buffer, 0); // need to have Combat item type added like 3 etc...
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeInt(buffer, 0);
						packet.writeShort(buffer, 0);
						packet.writeByte(buffer, 0);
						packet.writeInt(buffer, 0xFFFFFFFF);
						packet.writeInt(buffer, 0);
						packet.writeShort(buffer, 0);
						
					}

					// получаем индекс последняй занятой ячейки
					int last = inventory.getLastIndex();

					// перебираем ячейки инвенторя
					for(int i = 0; i < inventorySize; i++)
					{
						// получаем итем
						ItemInstance item = cells[i].getItem();

						// если его нет, пропускаем
						if(item == null)
							continue;

						packet.writeShort(buffer, n);

						n += 151;

						if(last == i)
							n = 0;

						packet.writeShort(buffer, n);
						packet.writeInt(buffer, 0);
						if(n !=0)
						{
						packet.writeShort(buffer, n - 2);
						}
						else
						{
							packet.writeShort(buffer,0);
						}
						packet.writeInt(buffer, item.getItemId());
						packet.writeInt(buffer, item.getObjectId());
						packet.writeInt(buffer, 0);// 0;
						packet.writeInt(buffer, player.getObjectId());
						packet.writeInt(buffer, player.getSubId());
						packet.writeInt(buffer, i + 20);
						packet.writeInt(buffer, 0);
						packet.writeLong(buffer, item.getItemCount());

						//packet.writeInt(buffer, 0);
						packet.writeByte(buffer, item.getBoundType() == BindType.NONE || item.isBinded()? 1 : 0);// This is Showing as 1E in real server this needs to be looked at to return the right values

						// получаем кристалы итема
						CrystalList crystals = item.getCrystals();

						// если их нет
						if(crystals == null || crystals.isEmpty())
						{
							packet.writeInt(buffer, 0); // 1 ячейка итема
							packet.writeInt(buffer, 0); // 2 ячейка итема
							packet.writeInt(buffer, 0); // 3 ячейка итема
							packet.writeInt(buffer, 0); // 4 ячейка итема
						}
						else
						{
							int diff = 4 - crystals.size();

							CrystalInstance[] array = crystals.getArray();

							for(int g = 0, length = crystals.size(); g < length; g++)
								packet.writeInt(buffer, array[g].getItemId());

							if(diff > 0)
								for(int g = 0; g < diff; g++)
									packet.writeInt(buffer, 0);
						}

						packet.writeByte(buffer, 0);
						packet.writeShort(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeShort(buffer, 0);
						packet.writeShort(buffer, 0); // need to have Combat item type added like 3 etc...
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeLong(buffer, 0);
						packet.writeInt(buffer, 0);
						packet.writeShort(buffer, 0);
						packet.writeByte(buffer, 0);
						packet.writeInt(buffer, 0xFFFFFFFF);
						packet.writeInt(buffer, 0);
						packet.writeShort(buffer, 0);
					}
				}
				finally
				{
					equipment.unlock();
				}
			}
			finally
			{
				inventory.unlock();
			}

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	/** промежуточный буффер */
	private ByteBuffer prepare;

	public S_Inven()
	{
		this.prepare = ByteBuffer.allocate(1024000).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.S_INVEN;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);

		// получаем промежуточный буффер
		ByteBuffer prepare = getPrepare();

		// переносим данные
		buffer.put(prepare.array(), 0, prepare.limit());
	}

	/**
	 * @return подготовленный буфер.
	 */
	public ByteBuffer getPrepare()
	{
		return prepare;
	}
}