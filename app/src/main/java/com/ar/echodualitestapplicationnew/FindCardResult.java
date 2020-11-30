package com.ar.echodualitestapplicationnew;

public class FindCardResult {
    private Long cardSerialNumber = -1L;
    private CardType cardType = CardType.No_Card;
    private byte[] cardId = null;

    public FindCardResult() {
    }

    public Long getCardSerialNumber() {
        return cardSerialNumber;
    }

    public void setCardSerialNumber(Long cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public byte[] getCardId() {
        return cardId;
    }

    public void setCardId(byte[] cardId) {
        this.cardId = cardId;
    }
}
