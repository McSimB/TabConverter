package org.herac.tuxguitar.player.base;

import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGMeasureHeader;
import org.herac.tuxguitar.song.models.TGSong;

public class MidiRepeatController {

	private int Coda;
	private int DoubleCoda;
	private int Segno;
	private int SegnoSegno;
	private int Fine;

	private int DaCapo;
	private int DaCapoAlCoda;
	private int DaCapoAlDoubleCoda;
	private int DaCapoAlFine;
	private int DaSegno;
	private int DaSegnoAlCoda;
	private int DaSegnoAlDoubleCoda;
	private int DaSegnoAlFine;
	private int DaSegnoSegno;
	private int DaSegnoSegnoAlCoda;
	private int DaSegnoSegnoAlDoubleCoda;
	private int DaSegnoSegnoAlFine;
	private int DaCoda;
	private int DaDoubleCoda;

	private TGSong song;
	private int count;
	private int index;
	private int lastIndex;
	private boolean shouldPlay;
	private boolean repeatOpen;
	private long repeatStart;
	private long repeatEnd;
	private long repeatMove;
	private int repeatStartIndex;
	private int repeatNumber;
	private int repeatAlternative;
	private boolean fromCoda;
	private boolean fromDoubleCoda;
	private boolean isFine;

	public MidiRepeatController(TGSong song) {
		this.song = song;
		this.count = song.countMeasureHeaders();
		this.index = 0;
		this.lastIndex = -1;
		this.shouldPlay = true;
		this.repeatOpen = true;
		this.repeatAlternative = 0;
		this.repeatStart = TGDuration.QUARTER_TIME;
		this.repeatEnd = 0;
		this.repeatMove = 0;
		this.repeatStartIndex = 0;
		this.repeatNumber = 0;
		setSings(song.getSigns());
		setFromSings(song.getFromSigns());
	}

	public void setSings(int[] sings) {
		Coda = sings[0];
		DoubleCoda = sings[1];
		Segno = sings[2];
		SegnoSegno = sings[3];
		Fine = sings[4];
	}

	public void setFromSings(int[] fromSings) {
		DaCapo = fromSings[0];
		DaCapoAlCoda = fromSings[1];
		DaCapoAlDoubleCoda = fromSings[2];
		DaCapoAlFine = fromSings[3];
		DaSegno = fromSings[4];
		DaSegnoAlCoda = fromSings[5];
		DaSegnoAlDoubleCoda = fromSings[6];
		DaSegnoAlFine = fromSings[7];
		DaSegnoSegno = fromSings[8];
		DaSegnoSegnoAlCoda = fromSings[9];
		DaSegnoSegnoAlDoubleCoda = fromSings[10];
		DaSegnoSegnoAlFine = fromSings[11];
		DaCoda = fromSings[12];
		DaDoubleCoda = fromSings[13];
	}

	public void process() {
		TGMeasureHeader header = this.song.getMeasureHeader(this.index);

		//Я открываю повторение всегда для первых тактов.
		if (header.getNumber() == 1) {
			this.repeatStartIndex = this.index;
			this.repeatStart = header.getStart();
			this.repeatOpen = true;
		}
		//По умолчанию такт должен звучать
		this.shouldPlay = true;

		//Если есть новое повторение, я сохраняю индекс такта, где начинается повторение
		if (header.isRepeatOpen()) {
			this.repeatStartIndex = this.index;
			this.repeatStart = header.getStart();
			this.repeatOpen = true;

			//Если это первый раз, когда я прошел через эти такты,
			// я поставил номер повторения и альтернативный конец в ноль
			if (this.index > this.lastIndex) {
				this.repeatNumber = 0;
				this.repeatAlternative = 0;
			}
		} else {
			//Я проверяю, есть ли альтернативный открытый конец
			if (this.repeatAlternative == 0) {
				this.repeatAlternative = header.getRepeatAlternative();
			}
			//Если я нахожусь в альтернативном конце, такт может звучать только в том случае,
			//если число повторений соответствует альтернативному окончательному номеру.
			if (this.repeatOpen && (this.repeatAlternative > 0) && ((this.repeatAlternative & (1 << (this.repeatNumber))) == 0)) {
				this.repeatMove -= header.getLength();
				if (header.getRepeatClose() > 0) {
					this.repeatAlternative = 0;
				}
				this.shouldPlay = false;
				this.index++;
				return;
			}
		}
		//перед выполнением возможного повторения я удерживаю индекс последнего такта
		this.lastIndex = Math.max(this.lastIndex, this.index);

		//если есть повторение, я делаю это
		if (this.repeatOpen && header.getRepeatClose() > 0) {
			if (this.repeatNumber < header.getRepeatClose() || (this.repeatAlternative > 0)) {
				this.repeatEnd = header.getStart() + header.getLength();
				this.repeatMove += this.repeatEnd - this.repeatStart;
				this.index = this.repeatStartIndex - 1;
				this.repeatNumber++;
			} else {
				this.repeatStart = 0;
				this.repeatNumber = 0;
				this.repeatEnd = 0;
				this.repeatOpen = false;
			}
			this.repeatAlternative = 0;
		}

		this.index++;

		boolean fromSegno = false;
		boolean fromSegnoSegno = false;
		boolean fromCapo = false;

		if (this.index == this.DaSegno) {
			fromSegno = true;
			this.DaSegno = -1;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaSegnoAlCoda) {
			fromSegno = true;
			this.DaSegnoAlCoda = -1;
			this.fromCoda = true;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaSegnoAlDoubleCoda) {
			fromSegno = true;
			this.DaSegnoAlDoubleCoda = -1;
			this.fromDoubleCoda = true;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaSegnoAlFine) {
			fromSegno = true;
			this.DaSegnoAlFine = -1;
			this.isFine = true;
		} else if (this.index == this.DaSegnoSegno) {
			fromSegnoSegno = true;
			this.DaSegnoSegno = -1;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaSegnoSegnoAlCoda) {
			fromSegnoSegno = true;
			this.DaSegnoSegnoAlCoda = -1;
			this.fromCoda = true;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaSegnoSegnoAlDoubleCoda) {
			fromSegnoSegno = true;
			this.DaSegnoSegnoAlDoubleCoda = -1;
			this.fromDoubleCoda = true;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaSegnoSegnoAlFine) {
			fromSegnoSegno = true;
			this.DaSegnoSegnoAlFine = -1;
			this.isFine = true;
		} else if (this.index == this.DaCapo) {
			fromCapo = true;
			this.DaCapo = -1;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaCapoAlCoda) {
			fromCapo = true;
			this.DaCapoAlCoda = -1;
			this.fromCoda = true;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaCapoAlDoubleCoda) {
			fromCapo = true;
			this.DaCapoAlDoubleCoda = -1;
			this.fromDoubleCoda = true;
			if (checkFine()) this.isFine = true;
		} else if (this.index == this.DaCapoAlFine) {
			fromCapo = true;
			this.DaCapoAlFine = -1;
			this.isFine = true;
		}

		if (fromSegno) {
			this.index = this.Segno != -1 ? this.Segno - 1 : 0;
			this.repeatMove += header.getStart() + header.getLength() - this.song.getMeasureHeader(this.index).getStart();
		} else if (fromSegnoSegno) {
			this.index = this.SegnoSegno != -1 ? this.SegnoSegno - 1 : 0;
			this.repeatMove += header.getStart() + header.getLength() - this.song.getMeasureHeader(this.index).getStart();
		} else if (fromCapo) {
			this.index = 0;
			this.repeatMove += header.getStart() + header.getLength() - this.song.getMeasureHeader(this.index).getStart();
		} else if (this.index == this.DaCoda && this.fromCoda) {
			if (this.Coda != -1) {
				this.index = this.Coda - 1;
				this.repeatMove += header.getStart() + header.getLength() -
						this.song.getMeasureHeader(this.Coda - 1).getStart();
				this.Coda = -1;
			}
		} else if (this.index == this.DaDoubleCoda && this.fromDoubleCoda) {
			if (this.DoubleCoda != -1) {
				this.index = this.DoubleCoda - 1;
				this.repeatMove += header.getStart() + header.getLength() -
						this.song.getMeasureHeader(this.DoubleCoda - 1).getStart();
				this.DoubleCoda = -1;
			}
		} else if (this.index == this.Fine && this.isFine) {
			this.index = this.count;
		} else if (this.index == this.Fine && checkAllFromSigns()) {
			this.index = this.count;
		}
	}

	public boolean finished() {
		return (this.index >= this.count);
	}

	public boolean shouldPlay() {
		return this.shouldPlay;
	}

	public int getIndex() {
		return this.index;
	}

	public long getRepeatMove() {
		return this.repeatMove;
	}

	private boolean checkFine() {
		return (this.DaSegnoAlFine == -1 && this.DaCapoAlFine == -1 && this.DaSegnoSegnoAlFine == -1);
	}

	private boolean checkAllFromSigns() {
		return (this.DaCapo == -1 && this.DaCapoAlCoda == -1 && this.DaCapoAlDoubleCoda == -1 &&
				this.DaCapoAlFine == -1 && this.DaSegno == -1 && this.DaSegnoAlCoda == -1 &&
				this.DaSegnoAlDoubleCoda == -1 && this.DaSegnoAlFine == -1 && this.DaSegnoSegno == -1 &&
				this.DaSegnoSegnoAlCoda == -1 && this.DaSegnoSegnoAlDoubleCoda == -1 &&
				this.DaSegnoSegnoAlFine == -1 && this.DaCoda == -1 && this.DaDoubleCoda == -1);
	}
}